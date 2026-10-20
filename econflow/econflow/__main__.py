"""CLI: python -m econflow {update,export,unmapped,serve}"""
import argparse
import functools
import http.server
from datetime import date, timedelta
from pathlib import Path

from . import dts, export, store
from .sectors import classify

ROOT = Path(__file__).resolve().parent.parent
DB = ROOT / "data" / "econflow.db"
JSON = ROOT / "data" / "flows.json"
DEFAULT_START = "2022-01-01"


def cmd_update(a):
    db = store.connect(a.db)
    last = store.last_date(db)
    since = a.since or (
        (date.fromisoformat(last) + timedelta(days=1)).isoformat() if last else DEFAULT_START
    )
    print(f"fetching DTS since {since} ...")
    n = store.upsert_flows(db, dts.parse_flow_rows(dts.fetch(dts.FLOWS_EP, since)))
    m = store.upsert_tga(db, dts.parse_balance_rows(dts.fetch(dts.BALANCE_EP, since)))
    db.commit()
    print(f"flows: {n} rows, tga: {m} rows, last date: {store.last_date(db)}")
    cmd_export(a)


def cmd_export(a):
    data = export.write(store.connect(a.db), a.out)
    print(f"wrote {a.out} ({len(data['dates'])} days, {len(data['categories'])} line items)")


def cmd_unmapped(a):
    db = store.connect(a.db)
    tot = {}
    for d, label, amt in db.execute("SELECT direction, label, amt FROM flow"):
        if classify(d, label) == "OTHER":
            tot[(d, label)] = tot.get((d, label), 0.0) + amt
    for (d, label), amt in sorted(tot.items(), key=lambda kv: -kv[1]):
        print(f"{d:3} {amt / 1e3:12,.1f} bn  {label}")


def cmd_serve(a):
    handler = functools.partial(http.server.SimpleHTTPRequestHandler, directory=str(ROOT))
    print(f"open http://localhost:{a.port}/web/")
    http.server.ThreadingHTTPServer(("127.0.0.1", a.port), handler).serve_forever()


def main():
    p = argparse.ArgumentParser(prog="econflow")
    p.add_argument("--db", default=str(DB))
    p.add_argument("--out", default=str(JSON))
    sub = p.add_subparsers(required=True)
    u = sub.add_parser("update", help="fetch new DTS days, then export")
    u.add_argument("--since", help=f"re-fetch from this date (default: day after last stored, or {DEFAULT_START})")
    u.set_defaults(fn=cmd_update)
    sub.add_parser("export", help="re-apply sector mapping, write flows.json").set_defaults(fn=cmd_export)
    sub.add_parser("unmapped", help="list line items still classified OTHER").set_defaults(fn=cmd_unmapped)
    s = sub.add_parser("serve", help="serve the web view")
    s.add_argument("--port", type=int, default=8000)
    s.set_defaults(fn=cmd_serve)
    a = p.parse_args()
    a.fn(a)


if __name__ == "__main__":
    main()

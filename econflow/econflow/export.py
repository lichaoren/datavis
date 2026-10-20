"""Build the compact JSON the web view reads."""
import json
from datetime import datetime, timezone

from .sectors import SECTORS, classify, is_debt


def build(db):
    cats, cat_idx = [], {}
    by_date = {}
    for date, d, label, amt in db.execute(
        "SELECT record_date, direction, label, amt FROM flow ORDER BY record_date"
    ):
        sector = classify(d, label)
        if sector is None:
            continue
        k = (d, label)
        if k not in cat_idx:
            cat_idx[k] = len(cats)
            cats.append({"name": label, "dir": d, "sector": sector, "debt": is_debt(label)})
        row = by_date.setdefault(date, {})
        i = cat_idx[k]
        row[i] = row.get(i, 0.0) + amt

    tga = dict(db.execute("SELECT record_date, close_bal FROM tga"))
    dates = sorted(by_date)
    return {
        "generated": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "source": "US Treasury Daily Treasury Statement, Table II (fiscaldata.treasury.gov)",
        "unit": "USD millions",
        "sectors": [{"id": i, "name": n, "color": c, "colorDark": cd} for i, n, c, cd in SECTORS],
        "categories": cats,
        "dates": dates,
        # per date: [[categoryIndex, amount], ...]
        "flows": [[[i, round(a, 3)] for i, a in sorted(by_date[x].items())] for x in dates],
        "tga": [tga.get(x) for x in dates],
    }


def write(db, path):
    data = build(db)
    with open(path, "w") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"))
    return data

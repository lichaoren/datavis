"""Fetch the US Treasury Daily Treasury Statement (DTS) from the Fiscal Data API.

No API key needed. Amounts are in USD millions.
Docs: https://fiscaldata.treasury.gov/datasets/daily-treasury-statement/
"""
import json
import time
import urllib.parse
import urllib.request

BASE = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service"
FLOWS_EP = "/v1/accounting/dts/deposits_withdrawals_operating_cash"
BALANCE_EP = "/v1/accounting/dts/operating_cash_balance"
PAGE_SIZE = 10000


def _get(url, retries=4):
    for i in range(retries + 1):
        try:
            with urllib.request.urlopen(url, timeout=60) as r:
                return json.load(r)
        except Exception:
            if i == retries:
                raise
            time.sleep(2 ** (i + 1))


def fetch(endpoint, since, until=None):
    """Yield all rows of `endpoint` with since <= record_date [<= until]."""
    flt = f"record_date:gte:{since}"
    if until:
        flt += f",record_date:lte:{until}"
    page = 1
    while True:
        q = urllib.parse.urlencode({
            "filter": flt,
            "sort": "record_date",
            "page[size]": PAGE_SIZE,
            "page[number]": page,
        })
        body = _get(f"{BASE}{endpoint}?{q}")
        yield from body.get("data", [])
        if page >= int(body.get("meta", {}).get("total-pages", 1) or 1):
            return
        page += 1


def num(v):
    """API returns amounts as strings; 'null' / '' mean missing."""
    if v in (None, "", "null"):
        return None
    return float(v)


def parse_flow_rows(rows):
    """Raw API rows -> (date, direction, line, label, amt). direction is 'in' | 'out'."""
    seen = {}
    for r in rows:
        tt = (r.get("transaction_type") or "").lower()
        if tt.startswith("deposit"):
            d = "in"
        elif tt.startswith("withdrawal"):
            d = "out"
        else:
            continue
        amt = num(r.get("transaction_today_amt"))
        if amt is None:
            continue
        catg = (r.get("transaction_catg") or "").strip()
        desc = (r.get("transaction_catg_desc") or "").strip()
        catg = "" if catg == "null" else catg
        desc = "" if desc == "null" else desc
        if not catg:
            catg, desc = desc, ""
        label = f"{catg} — {desc}" if desc else catg
        if not label:  # unlabeled row = daily total
            continue
        key = (r["record_date"], d)
        line = r.get("src_line_nbr")
        if line in (None, "", "null"):
            line = seen.get(key, 0)
        seen[key] = seen.get(key, 0) + 1
        yield r["record_date"], d, str(line), label, amt


def parse_balance_rows(rows):
    """Raw API rows -> (date, TGA closing balance).

    Since 2022-04-18: row 'Treasury General Account (TGA) Closing Balance', value
    in open_today_bal. Before: row 'Treasury General Account (TGA)', close_today_bal.
    """
    for r in rows:
        at = (r.get("account_type") or "").strip().lower()
        if "closing balance" in at:
            v = num(r.get("close_today_bal"))
            v = num(r.get("open_today_bal")) if v is None else v
        elif at == "treasury general account (tga)":
            v = num(r.get("close_today_bal"))
        else:
            continue
        if v is not None:
            yield r["record_date"], v

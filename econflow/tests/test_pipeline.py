import unittest

from econflow import dts, export, store
from econflow.sectors import classify


def row(date, tt, catg, amt, line, desc="null"):
    # Shape of /accounting/dts/deposits_withdrawals_operating_cash rows.
    return {"record_date": date, "account_type": "Treasury General Account (TGA)",
            "transaction_type": tt, "transaction_catg": catg, "transaction_catg_desc": desc,
            "transaction_today_amt": str(amt), "src_line_nbr": str(line)}


FLOWS = [
    row("2026-01-02", "Deposits", "Taxes - Withheld Individual/FICA", 300, 1),
    row("2026-01-02", "Deposits", "Taxes - Corporate Income", 50, 2),
    row("2026-01-02", "Deposits", "Public Debt Cash Issues (Table IIIB)", 1000, 3),
    row("2026-01-02", "Deposits", "Sub-Total Deposits", 1350, 4),
    row("2026-01-02", "Withdrawals", "SSA - Benefits Payments", 200, 1),
    row("2026-01-02", "Withdrawals", "Defense Vendor Payments (EFT)", 80, 2),
    row("2026-01-02", "Withdrawals", "HHS - Grants to States for Medicaid", 40, 3),
    row("2026-01-02", "Withdrawals", "Public Debt Cash Redemp. (Table IIIB)", 900, 4),
    row("2026-01-02", "Withdrawals", "Interest on Treasury Securities", 30, 5),
    row("2026-01-02", "Withdrawals", "Some Agency", "null", 6),
    row("2026-01-02", "Withdrawals", "null", 1250, 7),  # unlabeled daily total
]
BAL = [
    {"record_date": "2026-01-01", "account_type": "Treasury General Account (TGA) Closing Balance",
     "open_today_bal": "800000", "close_today_bal": "null"},
    {"record_date": "2026-01-02", "account_type": "Treasury General Account (TGA) Closing Balance",
     "open_today_bal": "800100", "close_today_bal": "null"},
    {"record_date": "2026-01-02", "account_type": "Treasury General Account (TGA) Opening Balance",
     "open_today_bal": "800000"},
    # pre-2022-04 format
    {"record_date": "2022-03-01", "account_type": "Treasury General Account (TGA)",
     "open_today_bal": "771264", "close_today_bal": "699674"},
    {"record_date": "2022-03-01", "account_type": "Supplementary Financing Program Account",
     "open_today_bal": "0", "close_today_bal": "0"},
]


class Pipeline(unittest.TestCase):
    def test_classify(self):
        cases = {
            ("in", "Taxes - Withheld Individual/FICA"): "HH",
            ("in", "Taxes - Non Withheld Ind/SECA Electronic"): "HH",
            ("in", "Taxes - Corporate Income"): "FIRM",
            ("in", "Taxes - Federal Unemployment (FUTA)"): "FIRM",
            ("in", "Customs and Certain Excise Taxes"): "FIRM",
            ("in", "Federal Reserve Earnings"): "FED",
            ("out", "SSA - Benefits Payments"): "HH",
            ("out", "IRS - Tax Refunds Individual (EFT)"): "HH",
            ("out", "IRS - Tax Refunds Business (EFT)"): "FIRM",
            ("out", "DoD - Military Active Duty Pay (EFT)"): "HH",
            ("out", "Defense Vendor Payments (EFT)"): "FIRM",
            ("out", "HHS - Grants to States for Medicaid"): "SL",
            ("out", "Interest on Treasury Securities"): "FIN",
            ("out", "Dept of State (DOS)"): "ROW",
            ("out", "Sub-Total Withdrawals"): None,
            ("out", "HHS - Federal Supple Med Insr Trust Fund"): "HH",
            ("out", "HHS - Temp Assistance for Needy Families"): "SL",
            ("out", "IAP - Agency for Int'l Development (AID)"): "ROW",
            ("out", "Unclassified"): "OTHER",
            ("out", "HHS - National Institutes of Health"): "FIRM",  # fallback
            ("in", "Postal Service"): "OTHER",  # fallback
            ("in", "Transfers from TGA (Table V)"): None,
        }
        for (d, label), want in cases.items():
            self.assertEqual(classify(d, label), want, label)

    def test_end_to_end(self):
        db = store.connect(":memory:")
        store.upsert_flows(db, dts.parse_flow_rows(FLOWS))
        store.upsert_tga(db, dts.parse_balance_rows(BAL))
        self.assertEqual(store.last_date(db), "2026-01-02")
        out = export.build(db)

        self.assertEqual(out["dates"], ["2026-01-02"])
        names = [c["name"] for c in out["categories"]]
        self.assertNotIn("Sub-Total Deposits", names)
        self.assertNotIn("Some Agency", names)  # null amount dropped
        debt = {c["name"] for c in out["categories"] if c["debt"]}
        self.assertEqual(len(debt), 2)

        # Conservation: inflows - outflows == ΔTGA.
        cats = out["categories"]
        net = sum(a if cats[i]["dir"] == "in" else -a for i, a in out["flows"][0])
        self.assertEqual(net, 1350 - 1250)
        self.assertEqual(out["tga"], [800100.0])
        self.assertEqual(dict(db.execute("SELECT * FROM tga"))["2022-03-01"], 699674.0)
        self.assertEqual(net, 800100 - 800000)

    def test_upsert_idempotent(self):
        db = store.connect(":memory:")
        store.upsert_flows(db, dts.parse_flow_rows(FLOWS))
        store.upsert_flows(db, dts.parse_flow_rows(FLOWS))
        self.assertEqual(db.execute("SELECT count(*) FROM flow").fetchone()[0], 9)


if __name__ == "__main__":
    unittest.main()

"""Map DTS line items to the counterparty sector of the federal government (G).

Every DTS deposit is money moving sector -> G (into the TGA); every withdrawal
is G -> sector. Rules are ordered regexes on the line label, first match wins.
Edit them here and re-run `export` — no re-fetch needed. `python -m econflow
unmapped` lists what still falls into OTHER.

Conventions (SFC-style, not cash-recipient-literal):
- Medicare / Medicaid benefits are social transfers in kind -> HH, except
  Medicaid & other grants to states -> SL (states then spend them).
- Interest on Treasuries & debt issues/redemptions -> FIN (bond holders, incl.
  foreign holders, which DTS cannot separate).
"""
import re

SECTORS = [
    # id, name, colour slot (light, dark) from the reference palette
    ("HH", "家庭 Households", "#2a78d6", "#3987e5"),
    ("FIRM", "企业 Firms", "#eb6834", "#d95926"),
    ("SL", "州/地方政府 State & Local", "#1baf7a", "#199e70"),
    ("FIN", "金融/债券持有人 Financial", "#eda100", "#c98500"),
    ("FED", "美联储 Fed", "#e87ba4", "#d55181"),
    ("ROW", "海外 Rest of World", "#008300", "#008300"),
    ("OTHER", "未归类 Other", "#898781", "#898781"),
]

# Lines that are not flows between G and another sector.
EXCLUDE = re.compile(
    r"^\s*(sub-?total|total)\b"
    r"|transfers? (to|from) (depositaries|federal reserve account|tt&l)"
    r"|\(table ii\)",
    re.I,
)

# Gross debt rollover lines (frontend can net them).
DEBT = re.compile(r"public debt cash (issues|redemp)", re.I)

_RULES = {
    "in": [
        (r"public debt cash issues", "FIN"),
        (r"federal reserve earnings", "FED"),
        (r"corporate", "FIRM"),
        (r"futa|federal unemployment", "FIRM"),
        (r"withheld|seca|fica|estate and gift|individual", "HH"),
        (r"customs|excise|tariff", "FIRM"),
        (r"foreign military|foreign|international|\bimf\b", "ROW"),
        (r"medicare premium|student loan|education|\bhud\b|housing", "HH"),
        (r"fdic|deposit insurance|tarp|federal financing bank|\bgse", "FIN"),
        (r"agriculture|usda|commodity credit|small business|\bsba\b|energy", "FIRM"),
        (r"unemployment trust|state", "SL"),
        (r"\btax", "HH"),
    ],
    "out": [
        (r"public debt cash redemp", "FIN"),
        (r"interest on (treasury|public debt)", "FIN"),
        (r"federal reserve", "FED"),
        (r"dept of state|department of state|international|foreign|usaid|\bimf\b", "ROW"),
        (r"refunds? business|business tax refund", "FIRM"),
        (r"medicaid|grants to states|highway|transit|\btanf\b|child nutrition"
         r"|title i\b|state unemployment|to states", "SL"),
        (r"social security|\bssa\b|supplemental security|medicare|\bsnap\b|nutrition"
         r"|veterans|\bva\b|unemployment|refunds? individual|individual tax refund"
         r"|salar|pay\b|payroll|retirement|\bopm\b|annuit|pell|student|housing|\bhud\b"
         r"|earned income|child tax credit|federal employees insurance|health ins"
         r"|hospital|education", "HH"),
        (r"vendor|defense|\bdod\b|contract|nasa|energy|transportation|commodity credit"
         r"|agriculture|usda|small business|\bsba\b|homeland|justice|interior", "FIRM"),
        (r"fdic|deposit insurance|federal financing bank|tarp", "FIN"),
    ],
}
_RULES = {d: [(re.compile(p, re.I), s) for p, s in rs] for d, rs in _RULES.items()}


def classify(direction, label):
    """-> sector id, or None if the line must be excluded."""
    if EXCLUDE.search(label):
        return None
    for rx, sector in _RULES[direction]:
        if rx.search(label):
            return sector
    return "OTHER"


def is_debt(label):
    return bool(DEBT.search(label))

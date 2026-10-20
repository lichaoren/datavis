"""Map DTS line items to the counterparty sector of the federal government (G).

Every DTS deposit is money moving sector -> G (into the TGA); every withdrawal
is G -> sector. Rules are ordered regexes on the line label, first match wins.
Edit them here and re-run `export` — no re-fetch needed. `python -m econflow
unmapped` lists lines no rule matches (they get FALLBACK).

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
    r"|transfers? (to|from) (depositaries|federal reserve account|tt&l|tga)"
    r"|\(table ii\)",
    re.I,
)

# Gross debt rollover lines (frontend can net them).
DEBT = re.compile(r"public debt cash (issues|redemp)", re.I)

_ROW = (r"dept of state|department of state|state department|international|internat'?l|interntl"
        r"|int'l|foreign|usaid|\bimf\b|multilateral|reconstruction|development (assn|fund)"
        r"|security assistance|\bdsca\b|export-import|\bdfc\b|overseas private")
_FIN_REG = (r"fdic|deposit insurance|federal financing bank|federal finance bank|tarp|\bgse"
            r"|securities and exchange|\bsec\b|comptroller of the currency|\bncua\b"
            r"|national credit union|\bcftc\b|commodity futures|farm credit"
            r"|resolution funding|emergency capital investment|\besf\b")

_RULES = {
    "in": [
        (r"public debt cash issues", "FIN"),
        (r"federal reserve earnings", "FED"),
        (r"corporate", "FIRM"),
        (r"futa|federal unemployment|pension benefit guaranty", "FIRM"),
        (r"withheld|seca|fica|estate and gift|individual", "HH"),
        (r"customs|excise|tariff", "FIRM"),
        (_ROW, "ROW"),
        (_FIN_REG, "FIN"),
        (r"medicare premium|student loan|education|\bhud\b|housing|thrift savings"
         r"|federal employee insurance|veterans affairs|\btsa\b|transportation security", "HH"),
        (r"agriculture|usda|commodity credit|small business|\bsba\b|energy|marketplace receipts"
         r"|\bfcc\b|communications commission|universal service|lease sales|minerals"
         r"|federal trade commission", "FIRM"),
        (r"supplemental security income|unemployment trust|state", "SL"),
        (r"\btax", "HH"),
    ],
    "out": [
        (r"unclassified", "OTHER"),
        (r"public debt cash redemp", "FIN"),
        (r"interest on (treasury|public debt)", "FIN"),
        (r"federal reserve", "FED"),
        (_ROW, "ROW"),
        (r"refunds? business|business tax refund", "FIRM"),
        (r"medicaid|grants to states|highway|transit|\btanf\b|needy families|children (&|and) families"
         r"|child nutrition|title i\b|state unemployment|to states|coronavirus relief|district of columbia", "SL"),
        (r"social security|\bssa\b|supplemental security|medicare|med insr|\bsnap\b|nutrition"
         r"|veterans|\bva\b|unemployment|refunds? individual|individual tax refund|economic impact"
         r"|salar|pay\b|payroll|retirement|\bopm\b|annuit|pell|student|housing|\bhud\b|rental assistance"
         r"|earned income|child tax credit|federal employees insurance|health ins|marketplace payments"
         r"|rrb - benefit|pension benefit|indian health|hospital|education", "HH"),
        (_FIN_REG, "FIN"),
        (r"vendor|defense|\bdod\b|contract|nasa|energy|transportation|commodity credit"
         r"|agriculture|usda|small business|\bsba\b|homeland|justice|interior", "FIRM"),
    ],
}
_RULES = {d: [(re.compile(p, re.I), s) for p, s in rs] for d, rs in _RULES.items()}

# No rule matched. Withdrawals default to FIRM: agency operating spending is
# mostly purchases of goods & services (the G -> F flow in a 4-sector SFC model).
FALLBACK = {"in": "OTHER", "out": "FIRM"}


def rule_sector(direction, label):
    """-> sector id from an explicit rule, None if excluded, '' if no rule matched."""
    if EXCLUDE.search(label):
        return None
    for rx, sector in _RULES[direction]:
        if rx.search(label):
            return sector
    return ""


def classify(direction, label):
    """-> sector id, or None if the line must be excluded."""
    s = rule_sector(direction, label)
    return FALLBACK[direction] if s == "" else s


def is_debt(label):
    return bool(DEBT.search(label))

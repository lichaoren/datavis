"""Local SQLite store for raw DTS rows."""
import sqlite3
from pathlib import Path

SCHEMA = """
CREATE TABLE IF NOT EXISTS flow (
    record_date TEXT NOT NULL,
    direction   TEXT NOT NULL,   -- 'in' = deposit to TGA, 'out' = withdrawal
    line        TEXT NOT NULL,
    label       TEXT NOT NULL,
    amt         REAL NOT NULL,   -- USD millions
    PRIMARY KEY (record_date, direction, line)
);
CREATE TABLE IF NOT EXISTS tga (
    record_date TEXT PRIMARY KEY,
    close_bal   REAL NOT NULL
);
"""


def connect(path):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    db = sqlite3.connect(path)
    db.executescript(SCHEMA)
    return db


def last_date(db):
    return db.execute("SELECT max(record_date) FROM flow").fetchone()[0]


def upsert_flows(db, rows):
    cur = db.executemany("INSERT OR REPLACE INTO flow VALUES (?,?,?,?,?)", rows)
    return cur.rowcount


def upsert_tga(db, rows):
    cur = db.executemany("INSERT OR REPLACE INTO tga VALUES (?,?)", rows)
    return cur.rowcount

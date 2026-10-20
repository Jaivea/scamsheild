CREATE TABLE IF NOT EXISTS scans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    content TEXT NOT NULL,
    score INTEGER NOT NULL,
    risk_level TEXT NOT NULL,
    reasons TEXT NOT NULL,
    scanned_at TEXT NOT NULL DEFAULT (datetime('now'))
);
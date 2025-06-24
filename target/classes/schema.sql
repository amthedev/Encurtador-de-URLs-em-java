-- Create URL_MAPPING table
CREATE TABLE IF NOT EXISTS url_mapping (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    original_url TEXT NOT NULL,
    short_code TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT 1
);

-- Create CLICK_STATS table
CREATE TABLE IF NOT EXISTS click_stats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    url_mapping_id INTEGER NOT NULL,
    ip_address TEXT NOT NULL,
    referrer TEXT,
    user_agent TEXT,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (url_mapping_id) REFERENCES url_mapping(id)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_short_code ON url_mapping(short_code);
CREATE INDEX IF NOT EXISTS idx_original_url ON url_mapping(original_url);
CREATE INDEX IF NOT EXISTS idx_click_stats_url ON click_stats(url_mapping_id);

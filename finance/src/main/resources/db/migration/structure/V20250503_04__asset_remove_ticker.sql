ALTER TABLE assets DROP INDEX idx_assets_ticker_exchange;

ALTER TABLE assets
DROP COLUMN ticker;

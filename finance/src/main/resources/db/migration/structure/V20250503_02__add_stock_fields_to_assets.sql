ALTER TABLE assets
  ADD COLUMN ticker VARCHAR(10) NULL,
  ADD COLUMN quantity DECIMAL(19,4) NULL,
  ADD COLUMN stock_exchange VARCHAR(50) NULL;

CREATE INDEX idx_assets_ticker_exchange ON assets (ticker, stock_exchange);


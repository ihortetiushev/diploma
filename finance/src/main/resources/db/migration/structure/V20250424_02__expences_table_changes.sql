ALTER TABLE expenses_category
ADD CONSTRAINT uq_expenses_category_name UNIQUE (name);

ALTER TABLE expenses
ADD COLUMN asset_id BIGINT,
ADD CONSTRAINT fk_expenses_asset FOREIGN KEY (asset_id) REFERENCES assets(id);

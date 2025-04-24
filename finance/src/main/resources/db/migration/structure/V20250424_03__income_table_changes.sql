ALTER TABLE income_category
ADD CONSTRAINT uc_income_category_name UNIQUE (name);

ALTER TABLE income
ADD COLUMN asset_id BIGINT,
ADD CONSTRAINT fk_income_asset FOREIGN KEY (asset_id) REFERENCES assets(id);

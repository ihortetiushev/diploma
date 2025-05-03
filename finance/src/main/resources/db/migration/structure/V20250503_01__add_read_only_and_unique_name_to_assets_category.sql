ALTER TABLE assets_category
ADD COLUMN read_only BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO assets_category (name, read_only)
VALUES ('Акції', TRUE);

ALTER TABLE assets_category
ADD CONSTRAINT uq_assets_category_name UNIQUE (name);
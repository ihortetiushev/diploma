ALTER TABLE assets ADD COLUMN name VARCHAR(50);
UPDATE assets SET name = LEFT(description, 50);
ALTER TABLE assets MODIFY name VARCHAR(50) NOT NULL;
ALTER TABLE assets ADD CONSTRAINT uq_assets_name UNIQUE (name);

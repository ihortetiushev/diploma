-- Change `quantity` from DECIMAL to INTEGER
ALTER TABLE assets
MODIFY COLUMN quantity INT;

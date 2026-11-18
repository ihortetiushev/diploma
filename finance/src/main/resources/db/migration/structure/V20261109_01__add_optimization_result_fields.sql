ALTER TABLE optimization_categories
    ADD COLUMN optimal DECIMAL(19, 2),
    ADD COLUMN result VARCHAR(255),
    ADD COLUMN deviation DECIMAL(19, 2);

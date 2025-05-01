ALTER TABLE income
ADD COLUMN amount_main_currency DECIMAL(19, 2) NOT NULL;
ALTER TABLE expenses
ADD COLUMN amount_main_currency DECIMAL(19, 2) NOT NULL;
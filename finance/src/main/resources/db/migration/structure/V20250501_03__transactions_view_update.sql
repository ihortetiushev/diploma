CREATE OR REPLACE VIEW transaction_view AS
SELECT
    e.id AS id,
    'EXPENSE' AS type,
    e.operation_date AS operation_date,
    ec.name AS category,
    e.amount AS amount,
    e.amount_main_currency AS amount_main_currency,
    e.currency_code AS currency,
    e.asset_id AS asset_id,
    e.description AS description
FROM expenses e
JOIN expenses_category ec ON e.category_id = ec.id

UNION ALL

SELECT
    i.id AS id,
    'INCOME' AS type,
    i.operation_date AS operation_date,
    ic.name AS category,
    i.amount AS amount,
    i.amount_main_currency AS amount_main_currency,
    i.currency_code AS currency,
    i.asset_id AS asset_id,
    i.description AS description
FROM income i
JOIN income_category ic ON i.category_id = ic.id;

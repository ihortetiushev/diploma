CREATE TABLE monthly_budget_plan
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_year INT            NOT NULL,
    month     INT            NOT NULL,
    income    DECIMAL(19, 2) NOT NULL,
    expenses  DECIMAL(19, 2) NOT NULL,
    CONSTRAINT uk_monthly_budget_plan_year_month UNIQUE (plan_year, month)
);

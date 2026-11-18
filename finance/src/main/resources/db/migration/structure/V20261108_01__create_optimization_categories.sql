CREATE TABLE optimization_categories
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    spending_limit DECIMAL(19, 2),
    priority       INT,
    desired        DECIMAL(19, 2),
    sort_order     INT NOT NULL
);

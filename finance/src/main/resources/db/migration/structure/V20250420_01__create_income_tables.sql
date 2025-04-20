CREATE TABLE income_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE income (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    amount DECIMAL(19, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    operation_date DATE NOT NULL,
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_income_category FOREIGN KEY (category_id) REFERENCES income_category(id)
);
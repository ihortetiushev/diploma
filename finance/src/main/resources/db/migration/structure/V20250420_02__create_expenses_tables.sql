CREATE TABLE expenses_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE expenses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    amount DECIMAL(19, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    operation_date DATE NOT NULL,
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_expenses_category FOREIGN KEY (category_id) REFERENCES expenses_category(id)
);
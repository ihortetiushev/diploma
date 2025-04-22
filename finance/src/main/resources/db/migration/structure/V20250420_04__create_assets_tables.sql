CREATE TABLE assets_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    initial_value DECIMAL(19, 2) NOT NULL,
    current_value DECIMAL(19,2),
    currency_code CHAR(3) NOT NULL,
    last_valuated_date DATE,
    description VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    status ENUM('active', 'closed') DEFAULT 'active',
    CONSTRAINT fk_assets_category FOREIGN KEY (category_id) REFERENCES assets_category(id)
);
CREATE TABLE currency (
    currency_code VARCHAR(3)  PRIMARY KEY NOT NULL
);

insert into currency values ('UAH');
insert into currency values ('USD');
insert into currency values ('EUR');

ALTER TABLE income
ADD COLUMN currency_code VARCHAR(3) NOT NULL DEFAULT 'UAH',
ADD CONSTRAINT fk_income_currency FOREIGN KEY (currency_code) REFERENCES currency(currency_code);

ALTER TABLE expenses
ADD COLUMN currency_code VARCHAR(3) NOT NULL DEFAULT 'UAH',
ADD CONSTRAINT fk_expenses_currency FOREIGN KEY (currency_code) REFERENCES currency(currency_code);


CREATE DATABASE IF NOT EXISTS finance_tracker;
USE finance_tracker;

CREATE TABLE IF NOT EXISTS categories (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    date        DATE           NOT NULL,
    type        VARCHAR(20)    NOT NULL,
    category    VARCHAR(100)   NOT NULL,
    amount      DECIMAL(12, 2) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS budgets (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    category      VARCHAR(100)   NOT NULL,
    monthly_limit DECIMAL(12, 2) NOT NULL
);

INSERT INTO categories (name, type) VALUES
    ('Salary',        'Income'),
    ('Freelance',     'Income'),
    ('Business',      'Income'),
    ('Investments',   'Income'),
    ('Other Income',  'Income'),
    ('Food',          'Expense'),
    ('Transport',     'Expense'),
    ('Utilities',     'Expense'),
    ('Rent',          'Expense'),
    ('Healthcare',    'Expense'),
    ('Education',     'Expense'),
    ('Shopping',      'Expense'),
    ('Entertainment', 'Expense'),
    ('Other Expense', 'Expense');

INSERT INTO transactions (date, type, category, amount, description) VALUES
    (CURDATE() - INTERVAL 25 DAY, 'Income',  'Salary',        55000.00, 'Monthly salary - March'),
    (CURDATE() - INTERVAL 20 DAY, 'Expense', 'Rent',          12000.00, 'Monthly house rent'),
    (CURDATE() - INTERVAL 18 DAY, 'Expense', 'Food',           3200.00, 'Grocery shopping'),
    (CURDATE() - INTERVAL 15 DAY, 'Income',  'Freelance',      8000.00, 'Web design project'),
    (CURDATE() - INTERVAL 12 DAY, 'Expense', 'Transport',      1500.00, 'Monthly bus pass + auto'),
    (CURDATE() - INTERVAL 10 DAY, 'Expense', 'Utilities',      2200.00, 'Electricity + internet bill'),
    (CURDATE() - INTERVAL  8 DAY, 'Expense', 'Entertainment',  1800.00, 'OTT subscriptions + movie'),
    (CURDATE() - INTERVAL  5 DAY, 'Expense', 'Healthcare',     2500.00, 'Doctor visit + medicines'),
    (CURDATE() - INTERVAL  3 DAY, 'Expense', 'Shopping',       4500.00, 'Clothes and shoes'),
    (CURDATE() - INTERVAL  1 DAY, 'Expense', 'Food',           1200.00, 'Restaurant dinner');

INSERT INTO budgets (category, monthly_limit) VALUES
    ('Food',          5000.00),
    ('Transport',     2000.00),
    ('Entertainment', 2000.00),
    ('Shopping',      5000.00),
    ('Utilities',     3000.00);

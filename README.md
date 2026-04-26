# Smart Finance Tracker

A full-stack expense tracker and financial analytics web application built with Java Servlets, JDBC, MySQL, HTML, CSS, and JavaScript. It helps track income and expenses, manage category budgets, view dashboard summaries, analyze spending patterns, generate monthly reports, and export transaction data as CSV.

## Features

- Add, edit, delete, and filter income and expense transactions
- Dashboard with income, expense, and balance summaries
- Category-wise expense analytics
- Monthly income and expense trend charts
- Budget limits with spending status
- Monthly financial reports
- CSV export for transactions
- MySQL database schema with starter categories, sample transactions, and sample budgets

## Tech Stack

- Backend: Java Servlets, JDBC
- Database: MySQL
- Frontend: HTML, CSS, JavaScript
- Charts: Chart.js 
- Build tool: Maven
- Server: Apache Tomcat

## Project Structure

```text
.
|-- backend/
|   |-- config/
|   |   `-- web.xml
|   `-- java/
|       |-- *DAO.java
|       |-- *Servlet.java
|       |-- DBConnection.java
|       `-- model classes
|-- database/
|   `-- schema.sql
|-- frontend/
|   |-- assets/
|   |   |-- css/style.css
|   |   `-- js/app.js
|   `-- pages/
|       |-- index.html
|       |-- dashboard.html
|       |-- transactions.html
|       |-- budgets.html
|       |-- analytics.html
|       `-- report.html
|-- pom.xml
`-- README.md
```

## Prerequisites

- JDK 11 or newer
- Apache Maven
- Apache Tomcat
- MySQL Server
- MySQL client or MySQL Workbench

## Database Setup

1. Start MySQL.
2. Run the schema file:

```sql
SOURCE database/schema.sql;
```

Or open `database/schema.sql` in MySQL Workbench and run it.

This creates the `finance_tracker` database with:

- `categories`
- `transactions`
- `budgets`

## Configure Database Credentials

Set database credentials as environment variables before starting Tomcat:

```bash
export DB_URL="jdbc:mysql://localhost:3306/finance_tracker"
export DB_USER="finance_tracker_user"
export DB_PASSWORD=""
```

Do not commit real passwords to GitHub. Keep local credentials in your shell, Codespaces secrets, or a private `.env` file that is ignored by Git.

## Running the App

1. Build the project:

```bash
mvn clean package
```

2. Deploy the generated WAR file from `target/` to Tomcat's `webapps` folder.

3. Start Tomcat.

4. Open the app in your browser:

```text
http://localhost:8080/FinanceTracker/dashboard.html
```

The frontend JavaScript currently expects the backend at:

```text
http://localhost:8080/FinanceTracker
```

If your Tomcat context path or port is different, update the API URLs in `frontend/assets/js/app.js` and the page scripts that define report, budget, or export URLs.

## API Endpoints

### Transactions and Analytics

```text
GET    /api/transactions
GET    /api/transactions?type=Income&category=Salary
POST   /api/transactions
PUT    /api/transactions?id={id}
DELETE /api/transactions?id={id}
GET    /api/dashboard
GET    /api/categories
GET    /api/analytics/category
GET    /api/analytics/monthly
GET    /api/analytics/top-category
```

### Budgets

```text
GET    /budget/all
GET    /budget/status
POST   /budget/add
PUT    /budget/update?id={id}
DELETE /budget/delete?id={id}
```

### Reports and Export

```text
GET /report/months
GET /report/monthly?m=YYYY-MM
GET /export/csv
```

## What to Push to GitHub

Push these files and folders:

```text
backend/
database/
frontend/
pom.xml
README.md
.gitignore
```

Do not push generated or local-only files:

```text
target/
*.class
*.war
.idea/
.vscode/
*.iml
*.log
.env
```

Before pushing, make sure no real database password, token, or API key is present in tracked files.

## Suggested Git Commands

```bash
git init
git add backend database frontend pom.xml README.md .gitignore
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

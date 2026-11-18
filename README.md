# Banking System (Java, JavaFX, JDBC/H2)

Object-oriented banking system for individuals and companies with Savings, Investment and Cheque accounts.

## Architecture
| Layer | Package | Classes |
|-------|---------|---------|
| Core model | `bank.model` | `Customer` (abstract), `IndividualCustomer`, `CompanyCustomer`, `Account` (abstract), `SavingsAccount`, `InvestmentAccount`, `ChequeAccount`, `Transaction`, interfaces `InterestBearing` and `Withdrawable` |
| Boundary (GUI) | `bank.view` | `MainApp`, `LoginView`, `AccountView` (no business logic) |
| Controllers | `bank.controller` | `LoginController`, `AccountController`, `PasswordHasher` |
| Data access | `bank.dao` | `Database`, `CustomerDAO`, `AccountDAO`, `TransactionDAO` |

Business rules: Savings has no withdrawals (0.05% monthly interest); Investment needs BWP 500.00 to open (5% monthly interest); Cheque is for working individuals and needs employer name and address; an account cannot exist without a customer.

OOP principles: abstraction (`Account`, `Customer`), inheritance, interfaces, polymorphism (interest paid through `InterestBearing`), overriding (`payInterest`, `getAccountType`, `toString`) and overloading (`deposit(double)` / `deposit(double, String)`, `withdraw`, constructors).

## Running in GitHub Codespaces
1. Push this project to your GitHub repository (see the commit plan below) and open it in a Codespace. The `.devcontainer` folder installs Java 21, Maven and a browser-based desktop. If the Codespace was already open, run "Codespaces: Rebuild Container".
2. Run the tests: `mvn test`
3. Run the app: `mvn javafx:run`
4. A Codespace has no screen, so open the **Ports** tab, open port **6080** in the browser and enter the password `vscode`. The app window appears in that desktop.

The database is stored in the `data/` folder (H2 file database) and is created automatically.

## Suggested commit plan (one commit per assignment step)
```
git add pom.xml .gitignore .devcontainer src/main/java/bank/model src/test/java/bank/AccountModelTest.java
git commit -m "Step 4: core domain model with business logic and unit tests"
git add src/main/java/bank/view
git commit -m "Step 5: JavaFX boundary classes (LoginView, AccountView, MainApp)"
git add src/main/java/bank/controller
git commit -m "Step 6: controllers mediating between views and model"
git add src/main/java/bank/dao
git commit -m "Step 7: H2 schema and JDBC DAOs for CRUD"
git add src/test/java/bank/BankingIntegrationTest.java docs README.md
git commit -m "Step 8: integration tests and report"
git push
```

# Integration Test Report – Banking System

**Student:** ______________  **Date run:** ______________  **Environment:** GitHub Codespaces, Java 21, Maven, H2

## 1. Purpose and scope
These tests check that the modules work together as one application: the **JavaFX boundary classes** (`LoginView`, `AccountView`), the **controllers** (`LoginController`, `AccountController`), the **core domain model** (`Customer`, `Account` and subclasses) and the **JDBC data access layer** (`CustomerDAO`, `AccountDAO`, `TransactionDAO`) with an H2 database.

The automated tests (`BankingIntegrationTest`) call the controllers exactly as the views do, then read the database back through the DAOs to confirm what was saved. They use an in-memory H2 database that is reset before every test, so tests do not affect one another. The views contain no business logic and only forward input to the controllers, so the GUI screens were checked separately by the manual walkthrough in section 5.

## 2. How to run
```
mvn test
```
Results appear in the terminal and in `target/surefire-reports/`.

## 3. Automated integration tests
Fill the **Actual result** column from your own `mvn test` output (PASS / FAIL).

| ID | Test method | Flow tested | Expected result | Actual result |
|----|-------------|-------------|-----------------|---------------|
| IT1 | `IT1_fullUserFlow_...` | Register, log in, open Investment (1000), deposit 500, withdraw 200, pay interest | Balance 1300 before interest, interest 65.00 (5%), final balance 1365.00 in the database, 4 transactions saved | |
| IT2 | `IT2_dataSurvivesLogoutAndLogin` | Open Savings (250), log out, log in again | Account and balance 250.00 are loaded back from the database | |
| IT3 | `IT3_savingsWithdrawalIsRejected...` | Withdraw from a Savings account | Rejected; database balance stays 300.00 | |
| IT4 | `IT4_investmentBelowMinimum...` | Open Investment with 499.99 | Rejected; no account saved | |
| IT5 | `IT5_chequeAccountRequiresEmployer...` | Open Cheque with and without employer details | Rejected without details; saved as CHEQUE with details | |
| IT6 | `IT6_customerWithSeveralAccounts...` | Savings, Investment and Cheque for one customer, then pay interest | Savings 1000.50, Investment 1050.00, Cheque earns nothing | |
| IT7 | `IT7_wrongPasswordAndDuplicate...` | Wrong password; duplicate username | Both rejected with clear messages | |
| IT8 | `IT8_overdrawIsRejected...` | Withdraw more than the Investment balance | Rejected; database balance unchanged (600.00) | |

Unit tests for the model (`AccountModelTest`) cover deposit rules, the BWP 500 minimum, Savings having no withdrawal, employer details for Cheque, interest rates and polymorphism.

## 4. Summary of outcomes
Total tests run: ____  Passed: ____  Failed: ____

Defects found and fixed during testing (add rows, or write "none"):

| Defect | Cause | Fix |
|--------|-------|-----|
| | | |

## 5. Manual GUI walkthrough (end-to-end through the screens)
Run the app (`mvn javafx:run`, open the desktop on port 6080) and tick each step.

| Step | Action in the GUI | Expected | Result |
|------|-------------------|----------|--------|
| 1 | Register an individual customer | "Registration successful" and Login tab opens | |
| 2 | Log in with wrong password | Red message "Incorrect username or password" | |
| 3 | Log in with correct details | Accounts screen shows "Welcome, <name>" | |
| 4 | Open Investment with 400 | Message about the BWP 500.00 minimum | |
| 5 | Open Investment with 1000 | Account appears in the table; history shows the opening deposit | |
| 6 | Deposit 500, then withdraw 200 | Balance 1,300.00; two new history rows | |
| 7 | Open Savings and try to withdraw | "Savings accounts do not allow withdrawals" | |
| 8 | Press "Pay monthly interest" | Investment balance increases by 5%, Savings by 0.05% | |
| 9 | Log out and log in again | Same accounts and balances are shown | |

## 6. Conclusion
Write 3 or 4 sentences: did the modules integrate successfully, what the tests proved, and any limitations (for example, no automated tests of the JavaFX screens).

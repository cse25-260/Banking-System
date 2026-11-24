package bank.controller;

import bank.dao.AccountDAO;
import bank.dao.TransactionDAO;
import bank.model.Account;
import bank.model.BankException;
import bank.model.ChequeAccount;
import bank.model.Customer;
import bank.model.InterestBearing;
import bank.model.InvestmentAccount;
import bank.model.SavingsAccount;
import bank.model.Transaction;
import bank.model.Withdrawable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mediates between AccountView and the domain model. Views pass raw text in;
 * all validation, business rules and saving happen here or in the model.
 */
public class AccountController {
    public static final String DEFAULT_BRANCH = "Gaborone Main";

    private final Customer customer;
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public AccountController(Customer customer) throws BankException {
        this.customer = customer;
        refresh();
    }

    public Customer getCustomer() { return customer; }

    /** Reloads the customer's accounts from the database. */
    public void refresh() throws BankException {
        try {
            List<Account> loaded = accountDAO.findByCustomer(customer);
            customer.clearAccounts();
            for (Account a : loaded) customer.addAccount(a);
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    public List<Account> getAccounts() { return new ArrayList<>(customer.getAccounts()); }
    public double getTotalBalance() { return customer.getTotalBalance(); }

    public Account openAccount(String type, String initialDepositText,
                               String employerName, String employerAddress) throws BankException {
        if (type == null || type.isBlank()) throw new BankException("Please choose an account type");
        double initial = initialDepositText == null || initialDepositText.isBlank()
                ? 0 : parseAmount(initialDepositText);
        try {
            String number = accountDAO.nextAccountNumber();
            Account account;
            switch (type.trim().toUpperCase()) {
                case "SAVINGS":
                    account = SavingsAccount.open(number, customer, DEFAULT_BRANCH, initial);
                    break;
                case "INVESTMENT":
                    account = InvestmentAccount.open(number, customer, DEFAULT_BRANCH, initial);
                    break;
                case "CHEQUE":
                    account = ChequeAccount.open(number, customer, DEFAULT_BRANCH, initial, employerName, employerAddress);
                    break;
                default:
                    throw new BankException("Unknown account type: " + type);
            }
            accountDAO.insert(account);
            customer.addAccount(account);
            return account;
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    /** Returns the new balance. */
    public double deposit(String accountNumber, String amountText) throws BankException {
        Account account = requireAccount(accountNumber);
        double amount = parseAmount(amountText);
        account.deposit(amount);
        persist(account);
        return account.getBalance();
    }

    /** Returns the new balance. Savings accounts are rejected because they are not Withdrawable. */
    public double withdraw(String accountNumber, String amountText) throws BankException {
        Account account = requireAccount(accountNumber);
        if (!(account instanceof Withdrawable)) {
            throw new BankException("Savings accounts do not allow withdrawals");
        }
        double amount = parseAmount(amountText);
        ((Withdrawable) account).withdraw(amount);
        persist(account);
        return account.getBalance();
    }

    /** Pays monthly interest on every interest-bearing account. Returns the total paid. */
    public double payInterestOnAllAccounts() throws BankException {
        double total = 0;
        for (Account account : customer.getAccounts()) {
            if (account instanceof InterestBearing) {           // polymorphism: each type applies its own rate
                double paid = ((InterestBearing) account).payInterest();
                if (paid > 0) {
                    persist(account);
                    total += paid;
                }
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public List<Transaction> getTransactions(String accountNumber) throws BankException {
        requireAccount(accountNumber);
        try {
            return transactionDAO.findByAccount(accountNumber);
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    private void persist(Account account) throws BankException {
        try {
            accountDAO.updateBalanceAndLog(account, account.getLastTransaction());
        } catch (SQLException e) {
            refresh();   // memory and database must never disagree
            throw new BankException("The transaction could not be saved: " + e.getMessage(), e);
        }
    }

    private Account requireAccount(String accountNumber) throws BankException {
        Account account = customer.findAccount(accountNumber);
        if (account == null) throw new BankException("Account not found");
        return account;
    }

    private double parseAmount(String text) throws BankException {
        if (text == null || text.isBlank()) throw new BankException("Please enter an amount");
        try {
            return Double.parseDouble(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new BankException("Please enter a valid amount, e.g. 250.00");
        }
    }
}

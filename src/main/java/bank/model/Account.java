package bank.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Abstract bank account. An account can never exist without an owning customer. */
public abstract class Account {
    private final String accountNumber;
    private final Customer owner;
    private final String branch;
    protected double balance;
    private final List<Transaction> transactions = new ArrayList<>();

    protected Account(String accountNumber, Customer owner, String branch, double balance) {
        if (owner == null) {
            throw new IllegalArgumentException("An account cannot exist without a customer");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required");
        }
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.branch = branch == null ? "" : branch;
        this.balance = balance;
    }

    /** SAVINGS, INVESTMENT or CHEQUE. */
    public abstract String getAccountType();

    // ---- Overloaded deposit methods ----
    public void deposit(double amount) throws BankException {
        deposit(amount, "Deposit");
    }

    public void deposit(double amount, String description) throws BankException {
        credit(amount, "DEPOSIT", description);
    }

    /** Adds money to the balance and records it. Used by deposits and interest. */
    protected void credit(double amount, String type, String description) throws BankException {
        validateAmount(amount);
        balance = round2(balance + amount);
        record(type, amount, description);
    }

    /** Removes money from the balance and records it. Only Withdrawable accounts expose this. */
    protected void debit(double amount, String description) throws BankException {
        validateAmount(amount);
        if (amount > balance) {
            throw new BankException(String.format(
                    "Insufficient funds: balance is BWP %.2f but you tried to withdraw BWP %.2f", balance, amount));
        }
        balance = round2(balance - amount);
        record("WITHDRAWAL", amount, description);
    }

    /** Used by the open(...) factories to apply the first deposit. */
    protected void applyOpeningDeposit(double initial) throws BankException {
        if (initial < 0) throw new BankException("Opening deposit cannot be negative");
        if (initial > 0) deposit(initial, "Opening deposit");
    }

    private void record(String type, double amount, String description) {
        transactions.add(new Transaction(accountNumber, type, amount, balance, description));
    }

    protected static void validateAmount(double amount) throws BankException {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new BankException("Amount must be greater than 0");
        }
    }

    protected static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Transaction getLastTransaction() {
        return transactions.isEmpty() ? null : transactions.get(transactions.size() - 1);
    }

    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public String getAccountNumber() { return accountNumber; }
    public Customer getOwner() { return owner; }
    public String getBranch() { return branch; }
    public double getBalance() { return balance; }

    public String getTypeLabel() {
        String t = getAccountType();
        return t.charAt(0) + t.substring(1).toLowerCase();
    }

    @Override
    public String toString() {
        return String.format("%s account %s | owner: %s | branch: %s | balance: BWP %.2f",
                getTypeLabel(), accountNumber, owner.getDisplayName(), branch, balance);
    }
}

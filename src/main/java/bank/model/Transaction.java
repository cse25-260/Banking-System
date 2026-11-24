package bank.model;

import java.time.LocalDateTime;

/** One entry in an account's history. */
public class Transaction {
    private int id;
    private final String accountNumber;
    private final String type;          // DEPOSIT, WITHDRAWAL, INTEREST
    private final double amount;
    private final double balanceAfter;
    private final String description;
    private final LocalDateTime timestamp;

    public Transaction(String accountNumber, String type, double amount, double balanceAfter, String description) {
        this(accountNumber, type, amount, balanceAfter, description, LocalDateTime.now());
    }

    public Transaction(String accountNumber, String type, double amount, double balanceAfter,
                       String description, LocalDateTime timestamp) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s %s BWP %.2f (balance %.2f)", timestamp, type, amount, balanceAfter);
    }
}

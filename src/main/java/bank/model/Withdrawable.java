package bank.model;

/** Implemented by accounts that allow withdrawals (Savings deliberately does not). */
public interface Withdrawable {
    void withdraw(double amount) throws BankException;
}

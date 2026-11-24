package bank.model;

/** Savings: deposits and interest only. It does NOT implement Withdrawable. */
public class SavingsAccount extends Account implements InterestBearing {
    public static final double MONTHLY_INTEREST_RATE = 0.0005; // 0.05%

    /** Used when loading an existing account from the database (no checks, no transaction). */
    public SavingsAccount(String accountNumber, Customer owner, String branch, double balance) {
        super(accountNumber, owner, branch, balance);
    }

    /** Opens a brand new savings account. */
    public static SavingsAccount open(String accountNumber, Customer owner, String branch,
                                      double initialDeposit) throws BankException {
        SavingsAccount account = new SavingsAccount(accountNumber, owner, branch, 0);
        account.applyOpeningDeposit(initialDeposit);
        return account;
    }

    @Override public String getAccountType() { return "SAVINGS"; }
    @Override public double getMonthlyInterestRate() { return MONTHLY_INTEREST_RATE; }

    @Override
    public double payInterest() throws BankException {
        double interest = round2(getBalance() * MONTHLY_INTEREST_RATE);
        if (interest > 0) credit(interest, "INTEREST", "Monthly interest (0.05%)");
        return interest;
    }
}

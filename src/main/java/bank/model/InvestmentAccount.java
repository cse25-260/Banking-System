package bank.model;

/** Investment: deposits, withdrawals, 5% monthly interest, BWP 500.00 minimum opening deposit. */
public class InvestmentAccount extends Account implements InterestBearing, Withdrawable {
    public static final double MONTHLY_INTEREST_RATE = 0.05; // 5%
    public static final double MIN_OPENING_DEPOSIT = 500.00;

    public InvestmentAccount(String accountNumber, Customer owner, String branch, double balance) {
        super(accountNumber, owner, branch, balance);
    }

    public static InvestmentAccount open(String accountNumber, Customer owner, String branch,
                                         double initialDeposit) throws BankException {
        if (initialDeposit < MIN_OPENING_DEPOSIT) {
            throw new BankException(String.format(
                    "An Investment account needs an opening deposit of at least BWP %.2f", MIN_OPENING_DEPOSIT));
        }
        InvestmentAccount account = new InvestmentAccount(accountNumber, owner, branch, 0);
        account.applyOpeningDeposit(initialDeposit);
        return account;
    }

    @Override public String getAccountType() { return "INVESTMENT"; }
    @Override public double getMonthlyInterestRate() { return MONTHLY_INTEREST_RATE; }

    // ---- Overloaded withdraw methods ----
    @Override
    public void withdraw(double amount) throws BankException {
        withdraw(amount, "Withdrawal");
    }

    public void withdraw(double amount, String description) throws BankException {
        debit(amount, description);
    }

    @Override
    public double payInterest() throws BankException {
        double interest = round2(getBalance() * MONTHLY_INTEREST_RATE);
        if (interest > 0) credit(interest, "INTEREST", "Monthly interest (5%)");
        return interest;
    }
}

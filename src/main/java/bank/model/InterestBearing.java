package bank.model;

/** Implemented by accounts that earn monthly interest. */
public interface InterestBearing {
    /** Monthly rate as a fraction, e.g. 0.05 means 5%. */
    double getMonthlyInterestRate();

    /** Credits one month of interest and returns the amount paid. */
    double payInterest() throws BankException;
}

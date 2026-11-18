package bank.model;

/** Cheque: deposits and withdrawals, only for working individuals (employer details required). */
public class ChequeAccount extends Account implements Withdrawable {
    private final String employerName;
    private final String employerAddress;

    public ChequeAccount(String accountNumber, Customer owner, String branch, double balance,
                         String employerName, String employerAddress) {
        super(accountNumber, owner, branch, balance);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    public static ChequeAccount open(String accountNumber, Customer owner, String branch, double initialDeposit,
                                     String employerName, String employerAddress) throws BankException {
        if (!(owner instanceof IndividualCustomer)) {
            throw new BankException("A Cheque account can only be opened for a person who is working");
        }
        if (employerName == null || employerName.isBlank() || employerAddress == null || employerAddress.isBlank()) {
            throw new BankException("Employer name and employer address are required for a Cheque account");
        }
        ChequeAccount account = new ChequeAccount(accountNumber, owner, branch, 0,
                employerName.trim(), employerAddress.trim());
        account.applyOpeningDeposit(initialDeposit);
        return account;
    }

    public String getEmployerName() { return employerName; }
    public String getEmployerAddress() { return employerAddress; }

    @Override public String getAccountType() { return "CHEQUE"; }

    @Override
    public void withdraw(double amount) throws BankException {
        debit(amount, "Withdrawal");
    }

    @Override
    public String toString() {
        return super.toString() + " | employer: " + employerName;
    }
}

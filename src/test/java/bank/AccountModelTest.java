package bank;

import bank.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the core domain model (no GUI, no database). */
class AccountModelTest {
    private final Customer owner = new IndividualCustomer("Owen", "Kgosi", "Plot 12, Gaborone");

    @Test
    void depositIncreasesBalanceAndRecordsTransaction() throws BankException {
        Account acc = SavingsAccount.open("A1", owner, "Main", 100);
        acc.deposit(50.25);
        assertEquals(150.25, acc.getBalance(), 0.001);
        assertEquals("DEPOSIT", acc.getLastTransaction().getType());
        assertEquals(2, acc.getTransactions().size());           // opening deposit + deposit
    }

    @Test
    void overloadedDepositStoresDescription() throws BankException {
        Account acc = SavingsAccount.open("A1", owner, "Main", 0);
        acc.deposit(20, "Salary top-up");
        assertEquals("Salary top-up", acc.getLastTransaction().getDescription());
    }

    @Test
    void depositRejectsZeroAndNegativeAmounts() throws BankException {
        Account acc = SavingsAccount.open("A1", owner, "Main", 100);
        assertThrows(BankException.class, () -> acc.deposit(0));
        assertThrows(BankException.class, () -> acc.deposit(-10));
        assertEquals(100, acc.getBalance(), 0.001);
    }

    @Test
    void savingsAccountDoesNotAllowWithdrawals() throws BankException {
        Account acc = SavingsAccount.open("A1", owner, "Main", 100);
        assertFalse(acc instanceof Withdrawable);
    }

    @Test
    void investmentNeedsMinimumOpeningDeposit() throws BankException {
        assertThrows(BankException.class, () -> InvestmentAccount.open("A2", owner, "Main", 499.99));
        InvestmentAccount ok = InvestmentAccount.open("A2", owner, "Main", 500.00);
        assertEquals(500.00, ok.getBalance(), 0.001);
    }

    @Test
    void investmentWithdrawWorksAndRejectsOverdraw() throws BankException {
        InvestmentAccount acc = InvestmentAccount.open("A2", owner, "Main", 1000);
        acc.withdraw(300);
        assertEquals(700, acc.getBalance(), 0.001);
        assertThrows(BankException.class, () -> acc.withdraw(700.01));
        assertEquals(700, acc.getBalance(), 0.001);
    }

    @Test
    void chequeAccountNeedsEmployerDetailsAndAnIndividual() {
        assertThrows(BankException.class, () -> ChequeAccount.open("A3", owner, "Main", 0, "", ""));
        assertThrows(BankException.class,
                () -> ChequeAccount.open("A3", new CompanyCustomer("Acme Ltd", "Francistown"), "Main", 0, "X", "Y"));
    }

    @Test
    void chequeAccountAllowsDepositAndWithdrawal() throws BankException {
        ChequeAccount acc = ChequeAccount.open("A3", owner, "Main", 200, "Botswana Post", "Gaborone");
        acc.withdraw(50);
        assertEquals(150, acc.getBalance(), 0.001);
        assertEquals("Botswana Post", acc.getEmployerName());
    }

    @Test
    void accountCannotExistWithoutACustomer() {
        assertThrows(IllegalArgumentException.class, () -> new SavingsAccount("A4", null, "Main", 0));
    }

    @Test
    void interestRatesAreAppliedPerAccountType() throws BankException {
        SavingsAccount savings = SavingsAccount.open("S1", owner, "Main", 1000);
        InvestmentAccount investment = InvestmentAccount.open("I1", owner, "Main", 1000);
        assertEquals(0.50, savings.payInterest(), 0.001);       // 0.05%
        assertEquals(50.00, investment.payInterest(), 0.001);   // 5%
        assertEquals(1000.50, savings.getBalance(), 0.001);
        assertEquals(1050.00, investment.getBalance(), 0.001);
    }

    @Test
    void polymorphismPaysInterestOnlyOnInterestBearingAccounts() throws BankException {
        List<Account> accounts = new ArrayList<>();
        accounts.add(SavingsAccount.open("S1", owner, "Main", 2000));
        accounts.add(InvestmentAccount.open("I1", owner, "Main", 1000));
        accounts.add(ChequeAccount.open("C1", owner, "Main", 500, "Employer", "Addr"));
        double total = 0;
        for (Account a : accounts) {
            if (a instanceof InterestBearing) total += ((InterestBearing) a).payInterest();
        }
        assertEquals(51.00, total, 0.001);                      // 1.00 + 50.00, cheque earns none
    }

    @Test
    void customerCanHoldMultipleAccounts() throws BankException {
        owner.addAccount(SavingsAccount.open("S1", owner, "Main", 100));
        owner.addAccount(InvestmentAccount.open("I1", owner, "Main", 600));
        assertEquals(2, owner.getAccounts().size());
        assertEquals(700, owner.getTotalBalance(), 0.001);
        assertNotNull(owner.findAccount("I1"));
    }
}

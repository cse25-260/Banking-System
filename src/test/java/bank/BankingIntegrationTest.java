package bank;

import bank.controller.AccountController;
import bank.controller.LoginController;
import bank.dao.AccountDAO;
import bank.dao.Database;
import bank.dao.TransactionDAO;
import bank.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests: controller -> domain model -> JDBC DAOs -> H2 database.
 * The JavaFX views are thin and only call these same controller methods, so each test
 * follows the same path a user takes through the GUI.
 */
class BankingIntegrationTest {
    private LoginController login;

    @BeforeAll
    static void useInMemoryDatabase() {
        Database.setUrl("jdbc:h2:mem:banktest;DB_CLOSE_DELAY=-1");
    }

    @BeforeEach
    void freshDatabase() throws Exception {
        Database.reset();
        login = new LoginController();
    }

    private Customer registerAndLogin() throws BankException {
        login.registerIndividual("Owen", "Kgosi", "Plot 12, Gaborone", "71234567", "owenk", "secret123");
        return login.login("owenk", "secret123");
    }

    @Test
    void IT1_fullUserFlow_loginOpenDepositWithdrawInterest_isPersisted() throws Exception {
        Customer customer = registerAndLogin();
        AccountController controller = new AccountController(customer);

        Account inv = controller.openAccount("INVESTMENT", "1000", null, null);
        controller.deposit(inv.getAccountNumber(), "500");
        controller.withdraw(inv.getAccountNumber(), "200");
        double interest = controller.payInterestOnAllAccounts();

        assertEquals(65.00, interest, 0.001);                    // 5% of 1300

        List<Account> fromDb = new AccountDAO().findByCustomer(customer);
        assertEquals(1, fromDb.size());
        assertEquals(1365.00, fromDb.get(0).getBalance(), 0.001);

        List<Transaction> history = new TransactionDAO().findByAccount(inv.getAccountNumber());
        assertEquals(4, history.size());                          // opening, deposit, withdrawal, interest
        assertEquals("INTEREST", history.get(0).getType());       // newest first
    }

    @Test
    void IT2_dataSurvivesLogoutAndLogin() throws Exception {
        Customer first = registerAndLogin();
        new AccountController(first).openAccount("SAVINGS", "250", null, null);
        login.logout();

        Customer again = login.login("owenk", "secret123");
        AccountController controller = new AccountController(again);
        assertEquals(1, controller.getAccounts().size());
        assertEquals(250.00, controller.getTotalBalance(), 0.001);
    }

    @Test
    void IT3_savingsWithdrawalIsRejectedAndDatabaseUnchanged() throws Exception {
        Customer customer = registerAndLogin();
        AccountController controller = new AccountController(customer);
        Account sav = controller.openAccount("SAVINGS", "300", null, null);

        assertThrows(BankException.class, () -> controller.withdraw(sav.getAccountNumber(), "50"));
        assertEquals(300.00, new AccountDAO().findByNumber(sav.getAccountNumber()).getBalance(), 0.001);
    }

    @Test
    void IT4_investmentBelowMinimumIsRejectedAndNothingSaved() throws Exception {
        Customer customer = registerAndLogin();
        AccountController controller = new AccountController(customer);

        assertThrows(BankException.class, () -> controller.openAccount("INVESTMENT", "499.99", null, null));
        assertTrue(new AccountDAO().findByCustomer(customer).isEmpty());
    }

    @Test
    void IT5_chequeAccountRequiresEmployerDetails() throws Exception {
        Customer customer = registerAndLogin();
        AccountController controller = new AccountController(customer);

        assertThrows(BankException.class, () -> controller.openAccount("CHEQUE", "100", "", ""));
        Account cheque = controller.openAccount("CHEQUE", "100", "Botswana Post", "Gaborone");
        assertEquals("CHEQUE", new AccountDAO().findByNumber(cheque.getAccountNumber()).getAccountType());
    }

    @Test
    void IT6_customerWithSeveralAccountsGetsInterestPerType() throws Exception {
        Customer customer = registerAndLogin();
        AccountController controller = new AccountController(customer);
        Account sav = controller.openAccount("SAVINGS", "1000", null, null);
        Account inv = controller.openAccount("INVESTMENT", "1000", null, null);
        controller.openAccount("CHEQUE", "500", "Employer", "Addr");

        controller.payInterestOnAllAccounts();

        AccountDAO dao = new AccountDAO();
        assertEquals(1000.50, dao.findByNumber(sav.getAccountNumber()).getBalance(), 0.001);
        assertEquals(1050.00, dao.findByNumber(inv.getAccountNumber()).getBalance(), 0.001);
    }

    @Test
    void IT7_wrongPasswordAndDuplicateUsernameAreRejected() throws Exception {
        registerAndLogin();
        assertThrows(BankException.class, () -> login.login("owenk", "wrong-password"));
        assertThrows(BankException.class,
                () -> login.registerCompany("Acme Ltd", "Francistown", "", "owenk", "another123"));
    }

    @Test
    void IT8_overdrawIsRejectedAndBalanceUnchangedInDatabase() throws Exception {
        Customer customer = registerAndLogin();
        AccountController controller = new AccountController(customer);
        Account inv = controller.openAccount("INVESTMENT", "600", null, null);

        assertThrows(BankException.class, () -> controller.withdraw(inv.getAccountNumber(), "1000"));
        assertEquals(600.00, new AccountDAO().findByNumber(inv.getAccountNumber()).getBalance(), 0.001);
    }
}

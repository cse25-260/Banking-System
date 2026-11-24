package bank.controller;

import bank.dao.CustomerDAO;
import bank.dao.Database;
import bank.model.BankException;
import bank.model.CompanyCustomer;
import bank.model.Customer;
import bank.model.IndividualCustomer;

import java.sql.SQLException;

/** Handles registration, login and logout on behalf of the login/registration views. */
public class LoginController {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private Customer currentCustomer;
    private String lastFailedUser = "";
    private int failedAttempts = 0;

    /** Called once when the application starts. */
    public void initialiseSystem() throws BankException {
        try {
            Database.initSchema();
        } catch (SQLException e) {
            throw new BankException("Could not start the database: " + e.getMessage(), e);
        }
    }

    public Customer registerIndividual(String firstName, String surname, String address,
                                       String phone, String username, String password) throws BankException {
        require(firstName, "First name");
        require(surname, "Surname");
        validateCommon(address, username, password);
        return save(new IndividualCustomer(firstName, surname, address, phone, username.trim(), PasswordHasher.hash(password)));
    }

    public Customer registerCompany(String companyName, String address,
                                    String phone, String username, String password) throws BankException {
        require(companyName, "Company name");
        validateCommon(address, username, password);
        return save(new CompanyCustomer(companyName, address, phone, username.trim(), PasswordHasher.hash(password)));
    }

    public Customer login(String username, String password) throws BankException {
        require(username, "Username");
        require(password, "Password");
        String user = username.trim().toLowerCase();
        if (user.equals(lastFailedUser) && failedAttempts >= MAX_FAILED_ATTEMPTS) {
            throw new BankException("Too many failed attempts. Please restart the application and try again.");
        }
        try {
            Customer customer = customerDAO.findByUsername(username.trim());
            if (customer == null || !PasswordHasher.matches(password, customer.getPasswordHash())) {
                failedAttempts = user.equals(lastFailedUser) ? failedAttempts + 1 : 1;
                lastFailedUser = user;
                throw new BankException("Incorrect username or password");
            }
            failedAttempts = 0;
            lastFailedUser = "";
            currentCustomer = customer;
            return customer;
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    public void logout() { currentCustomer = null; }
    public Customer getCurrentCustomer() { return currentCustomer; }

    private Customer save(Customer customer) throws BankException {
        try {
            if (customerDAO.usernameExists(customer.getUsername())) {
                throw new BankException("That username is already taken");
            }
            customerDAO.insert(customer);
            return customer;
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    private void validateCommon(String address, String username, String password) throws BankException {
        require(address, "Address");
        require(username, "Username");
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BankException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    private void require(String value, String field) throws BankException {
        if (value == null || value.isBlank()) throw new BankException(field + " is required");
    }
}

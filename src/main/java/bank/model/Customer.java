package bank.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Abstract customer: either an individual or a company/organisation. */
public abstract class Customer {
    private int customerId;
    private String address;
    private String phone;
    private String username;
    private String passwordHash;
    private final List<Account> accounts = new ArrayList<>();

    protected Customer(String address, String phone, String username, String passwordHash) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        this.address = address.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /** Overloaded constructor for customers that are not yet registered with login details. */
    protected Customer(String address) {
        this(address, "", null, null);
    }

    public abstract String getDisplayName();

    /** INDIVIDUAL or COMPANY. */
    public abstract String getCustomerType();

    public void addAccount(Account account) {
        if (account != null && findAccount(account.getAccountNumber()) == null) {
            accounts.add(account);
        }
    }

    public Account findAccount(String accountNumber) {
        for (Account a : accounts) {
            if (a.getAccountNumber().equals(accountNumber)) return a;
        }
        return null;
    }

    public void clearAccounts() { accounts.clear(); }
    public List<Account> getAccounts() { return Collections.unmodifiableList(accounts); }

    public double getTotalBalance() {
        double total = 0;
        for (Account a : accounts) total += a.getBalance();
        return Math.round(total * 100.0) / 100.0;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }

    @Override
    public String toString() {
        return getDisplayName() + " (" + getCustomerType() + ", " + address + ")";
    }
}

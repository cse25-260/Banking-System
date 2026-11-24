package bank.dao;

import bank.model.Account;
import bank.model.ChequeAccount;
import bank.model.Customer;
import bank.model.InvestmentAccount;
import bank.model.SavingsAccount;
import bank.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for accounts. Balance changes and their transaction record are saved together. */
public class AccountDAO {

    /** Next unique account number from the database sequence. */
    public String nextAccountNumber() throws SQLException {
        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT NEXT VALUE FOR account_seq")) {
            rs.next();
            return "ACC" + rs.getLong(1);
        }
    }

    // ---- Create: saves the account and any transactions it already holds (e.g. opening deposit) ----
    public void insert(Account a) throws SQLException {
        String sql = "INSERT INTO accounts(account_number, account_type, customer_id, branch, balance, employer_name, employer_address) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, a.getAccountNumber());
                ps.setString(2, a.getAccountType());
                ps.setInt(3, a.getOwner().getCustomerId());
                ps.setString(4, a.getBranch());
                ps.setBigDecimal(5, BigDecimal.valueOf(a.getBalance()));
                if (a instanceof ChequeAccount) {
                    ps.setString(6, ((ChequeAccount) a).getEmployerName());
                    ps.setString(7, ((ChequeAccount) a).getEmployerAddress());
                } else {
                    ps.setNull(6, java.sql.Types.VARCHAR);
                    ps.setNull(7, java.sql.Types.VARCHAR);
                }
                ps.executeUpdate();
                for (Transaction t : a.getTransactions()) {
                    TransactionDAO.insert(con, t);
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---- Read ----
    public List<Account> findByCustomer(Customer owner) throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ? ORDER BY account_number";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, owner.getCustomerId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs, owner));
            }
        }
        return list;
    }

    public Account findByNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Customer owner = new CustomerDAO().findById(rs.getInt("customer_id"));
                return map(rs, owner);
            }
        }
    }

    // ---- Update: new balance and the transaction that caused it, all or nothing ----
    public void updateBalanceAndLog(Account a, Transaction t) throws SQLException {
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?")) {
                ps.setBigDecimal(1, BigDecimal.valueOf(a.getBalance()));
                ps.setString(2, a.getAccountNumber());
                ps.executeUpdate();
                if (t != null) TransactionDAO.insert(con, t);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---- Delete ----
    public void delete(String accountNumber) throws SQLException {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM accounts WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        }
    }

    private Account map(ResultSet rs, Customer owner) throws SQLException {
        String type = rs.getString("account_type");
        String number = rs.getString("account_number");
        String branch = rs.getString("branch");
        double balance = rs.getBigDecimal("balance").doubleValue();
        switch (type) {
            case "SAVINGS":
                return new SavingsAccount(number, owner, branch, balance);
            case "INVESTMENT":
                return new InvestmentAccount(number, owner, branch, balance);
            case "CHEQUE":
                return new ChequeAccount(number, owner, branch, balance,
                        rs.getString("employer_name"), rs.getString("employer_address"));
            default:
                throw new SQLException("Unknown account type: " + type);
        }
    }
}

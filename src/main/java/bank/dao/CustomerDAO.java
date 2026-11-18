package bank.dao;

import bank.model.CompanyCustomer;
import bank.model.Customer;
import bank.model.IndividualCustomer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** CRUD operations for customers. */
public class CustomerDAO {

    // ---- Create ----
    public void insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers(customer_type, first_name, surname, company_name, address, phone, username, password_hash) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCustomerType());
            if (c instanceof IndividualCustomer) {
                IndividualCustomer ind = (IndividualCustomer) c;
                ps.setString(2, ind.getFirstName());
                ps.setString(3, ind.getSurname());
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
                ps.setNull(3, java.sql.Types.VARCHAR);
                ps.setString(4, ((CompanyCustomer) c).getCompanyName());
            }
            ps.setString(5, c.getAddress());
            ps.setString(6, c.getPhone());
            ps.setString(7, c.getUsername());
            ps.setString(8, c.getPasswordHash());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setCustomerId(keys.getInt(1));
            }
        }
    }

    // ---- Read ----
    public Customer findById(int id) throws SQLException {
        return findOne("SELECT * FROM customers WHERE customer_id = ?", id);
    }

    public Customer findByUsername(String username) throws SQLException {
        return findOne("SELECT * FROM customers WHERE LOWER(username) = LOWER(?)", username);
    }

    public boolean usernameExists(String username) throws SQLException {
        return findByUsername(username) != null;
    }

    private Customer findOne(String sql, Object param) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // ---- Update ----
    public void update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET address = ?, phone = ? WHERE customer_id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getAddress());
            ps.setString(2, c.getPhone());
            ps.setInt(3, c.getCustomerId());
            ps.executeUpdate();
        }
    }

    // ---- Delete (accounts and transactions are removed by ON DELETE CASCADE) ----
    public void delete(int customerId) throws SQLException {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c;
        if ("COMPANY".equals(rs.getString("customer_type"))) {
            c = new CompanyCustomer(rs.getString("company_name"), rs.getString("address"),
                    rs.getString("phone"), rs.getString("username"), rs.getString("password_hash"));
        } else {
            c = new IndividualCustomer(rs.getString("first_name"), rs.getString("surname"),
                    rs.getString("address"), rs.getString("phone"),
                    rs.getString("username"), rs.getString("password_hash"));
        }
        c.setCustomerId(rs.getInt("customer_id"));
        return c;
    }
}

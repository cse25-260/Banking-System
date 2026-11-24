package bank.dao;

import bank.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Create/read operations for the transaction history (history is never edited). */
public class TransactionDAO {

    /** Inserts using an existing connection so it can share a database transaction. */
    static void insert(Connection con, Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions(account_number, txn_type, amount, balance_after, description, created_at) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getAccountNumber());
            ps.setString(2, t.getType());
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(t.getAmount()));
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(t.getBalanceAfter()));
            ps.setString(5, t.getDescription());
            ps.setTimestamp(6, Timestamp.valueOf(t.getTimestamp()));
            ps.executeUpdate();
        }
    }

    public List<Transaction> findByAccount(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_id DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction(
                            rs.getString("account_number"),
                            rs.getString("txn_type"),
                            rs.getBigDecimal("amount").doubleValue(),
                            rs.getBigDecimal("balance_after").doubleValue(),
                            rs.getString("description"),
                            rs.getTimestamp("created_at").toLocalDateTime());
                    t.setId(rs.getInt("transaction_id"));
                    list.add(t);
                }
            }
        }
        return list;
    }
}

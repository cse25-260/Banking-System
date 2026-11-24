package bank.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens connections to the embedded H2 database and creates the schema. */
public final class Database {
    private static String url = "jdbc:h2:./data/bankdb";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private Database() { }

    /** Lets tests switch to an in-memory database. */
    public static void setUrl(String newUrl) { url = newUrl; }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, USER, PASSWORD);
    }

    /** Creates all tables if they do not exist yet. */
    public static void initSchema() throws SQLException {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS customers ("
                    + "customer_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL','COMPANY')),"
                    + "first_name VARCHAR(60),"
                    + "surname VARCHAR(60),"
                    + "company_name VARCHAR(120),"
                    + "address VARCHAR(200) NOT NULL,"
                    + "phone VARCHAR(30),"
                    + "username VARCHAR(50) NOT NULL UNIQUE,"
                    + "password_hash VARCHAR(200) NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "account_number VARCHAR(20) PRIMARY KEY,"
                    + "account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('SAVINGS','INVESTMENT','CHEQUE')),"
                    + "customer_id INT NOT NULL,"
                    + "branch VARCHAR(60),"
                    + "balance DECIMAL(15,2) NOT NULL CHECK (balance >= 0),"
                    + "employer_name VARCHAR(120),"
                    + "employer_address VARCHAR(200),"
                    + "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE)");
            st.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "transaction_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "account_number VARCHAR(20) NOT NULL,"
                    + "txn_type VARCHAR(20) NOT NULL CHECK (txn_type IN ('DEPOSIT','WITHDRAWAL','INTEREST')),"
                    + "amount DECIMAL(15,2) NOT NULL,"
                    + "balance_after DECIMAL(15,2) NOT NULL,"
                    + "description VARCHAR(200),"
                    + "created_at TIMESTAMP NOT NULL,"
                    + "FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE)");
            st.execute("CREATE SEQUENCE IF NOT EXISTS account_seq START WITH 1000001");
        }
    }

    /** Wipes everything and recreates the schema. Intended for tests only (H2 specific). */
    public static void reset() throws SQLException {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute("DROP ALL OBJECTS");
        }
        initSchema();
    }
}

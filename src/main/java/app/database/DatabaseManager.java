package app.database;

import app.security.PasswordHasher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseManager {
    public static final String DEFAULT_DB_FILE = "fuel_app.db";

    private static String url = "jdbc:sqlite:" + DEFAULT_DB_FILE;
    private static boolean initialized = false;

    /** Points the app at a different database file. Must be called before init(). */
    public static synchronized void useDatabase(String fileName) {
        if (initialized) {
            throw new IllegalStateException("Database already initialized");
        }
        url = "jdbc:sqlite:" + fileName;
    }

    /** Creates tables and upgrades old data. Safe to call more than once. */
    public static synchronized void init() {
        if (!initialized) {
            initialized = true;
            initializeDatabase();
        }
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            createUsersTable(stmt);
            createAdminUsersTable(stmt);
            createFuelRequestsTable(stmt);

            migratePlaintextPasswords(conn, "users");
            migratePlaintextPasswords(conn, "admin_users");

            System.out.println("Database initialized successfully");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static void createUsersTable(Statement stmt) throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "first_name VARCHAR(50) NOT NULL, " +
                        "last_name VARCHAR(50) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL, " +
                        "id_number VARCHAR(20) NOT NULL, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password VARCHAR(100) NOT NULL, " +
                        "phone_number VARCHAR(20) NOT NULL, " +
                        "car_make VARCHAR(50), " +
                        "car_model VARCHAR(50), " +
                        "car_reg VARCHAR(20), " +
                        "role VARCHAR(20) DEFAULT 'customer', " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)";

        stmt.execute(sql);
    }

    private static void createAdminUsersTable(Statement stmt) throws SQLException {
        // No default admin is inserted - the first admin is created from the admin login screen
        String sql =
                "CREATE TABLE IF NOT EXISTS admin_users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "phone_number TEXT NOT NULL)";

        stmt.execute(sql);
    }

    private static void createFuelRequestsTable(Statement stmt) throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS fuel_requests (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "customer_name VARCHAR(100) NOT NULL, " +
                        "customer_phone VARCHAR(20) NOT NULL, " +
                        "vehicle_model VARCHAR(100) NOT NULL, " +
                        "registration VARCHAR(50) NOT NULL, " +
                        "fuel_type VARCHAR(20) NOT NULL, " +
                        "fuel_amount DECIMAL(10,2) NOT NULL, " +
                        "total_amount DECIMAL(10,2) NOT NULL, " +
                        "status VARCHAR(20) DEFAULT 'pending', " +
                        "request_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id))";

        stmt.execute(sql);
    }

    /**
     * One-time upgrade: replaces any plaintext passwords left from older versions with hashes.
     * Table names are fixed constants above, never user input.
     */
    private static void migratePlaintextPasswords(Connection conn, String table) throws SQLException {
        Map<Integer, String> plaintext = new LinkedHashMap<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, password FROM " + table)) {
            while (rs.next()) {
                String stored = rs.getString("password");
                if (!PasswordHasher.isHashed(stored)) {
                    plaintext.put(rs.getInt("id"), stored);
                }
            }
        }

        if (plaintext.isEmpty()) {
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE " + table + " SET password = ? WHERE id = ?")) {
            for (Map.Entry<Integer, String> row : plaintext.entrySet()) {
                pstmt.setString(1, PasswordHasher.hash(row.getValue()));
                pstmt.setInt(2, row.getKey());
                pstmt.executeUpdate();
            }
        }
        System.out.println("🔒 Hashed " + plaintext.size() + " plaintext password(s) in " + table);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}

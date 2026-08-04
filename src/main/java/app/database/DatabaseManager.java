package app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:fuel_app.db";

    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            createUsersTable(stmt);
            createFuelRequestsTable(stmt);

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

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
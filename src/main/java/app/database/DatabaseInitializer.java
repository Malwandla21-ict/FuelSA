package app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:fuel_app.db");
             Statement stmt = conn.createStatement()) {

            // Create tables if they don't exist
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS admin_users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    phone_number TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fuel_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_name TEXT NOT NULL,
                    customer_phone TEXT NOT NULL,
                    vehicle_model TEXT NOT NULL,
                    registration TEXT NOT NULL,
                    fuel_type TEXT NOT NULL,
                    fuel_amount REAL NOT NULL,
                    total_amount REAL NOT NULL,
                    status TEXT DEFAULT 'pending',
                    request_date DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Insert default admin if not exists
            stmt.execute("""
                INSERT OR IGNORE INTO admin_users (username, password, phone_number) 
                VALUES ('admin', 'admin123', '+27123456789')
            """);

            System.out.println("✅ Database initialized successfully!");

        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
        }
    }
}
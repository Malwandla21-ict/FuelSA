package app.database;

import app.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Demo mode: run the app with the "--demo" argument (mvn javafx:run -Pdemo).
 * Uses its own database file so test data never mixes with real customers,
 * and SMS sending is switched off so no real phone number is ever messaged.
 *
 * All people, phone numbers and ID numbers below are made up.
 *
 * Demo logins:
 *   Customers: thandi, sipho, lerato, pieter, ayesha, kagiso  - password: Demo@1234
 *   Admin:     admin                                         - password: FuelSA-demo-2026
 */
public final class DemoData {
    public static final String DEMO_DB_FILE = "fuel_app_demo.db";
    public static final String CUSTOMER_PASSWORD = "Demo@1234";
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "FuelSA-demo-2026";

    private static final double PETROL_PRICE = 20.15;
    private static final double DIESEL_PRICE = 19.10;

    private static boolean active = false;

    private DemoData() {}

    public static boolean isActive() {
        return active;
    }

    /** Switches to the demo database and fills it with sample data the first time. */
    public static void enable() {
        active = true;
        DatabaseManager.useDatabase(DEMO_DB_FILE);
        DatabaseManager.init();

        if (new UserDAO().countUsers() > 0) {
            return; // already seeded
        }

        try {
            seed();
            System.out.println("🧪 Demo data created in " + DEMO_DB_FILE);
        } catch (SQLException e) {
            System.err.println("❌ Could not create demo data: " + e.getMessage());
        }
    }

    private static void seed() throws SQLException {
        // {first, last, username, phone, make, model, reg}
        String[][] customers = {
                {"Thandi", "Mokoena", "thandi", "0820000101", "Toyota", "Corolla Cross", "CA 123-456"},
                {"Sipho", "Ndlovu", "sipho", "0820000102", "Ford", "Ranger", "ND 456-789"},
                {"Lerato", "Dlamini", "lerato", "0820000103", "Volkswagen", "Polo Vivo", "GP LD 22 GP"},
                {"Pieter", "van Wyk", "pieter", "0820000104", "Toyota", "Hilux", "CAW 98-765"},
                {"Ayesha", "Patel", "ayesha", "0820000105", "Hyundai", "Tucson", "AP 77 KZN"},
                {"Kagiso", "Molefe", "kagiso", "0820000106", "Nissan", "NP200", "KM 31 NW"},
        };

        UserDAO userDAO = new UserDAO();
        int[] userIds = new int[customers.length];
        for (int i = 0; i < customers.length; i++) {
            String[] c = customers[i];
            User u = new User();
            u.setFirstName(c[0]);
            u.setLastName(c[1]);
            u.setEmail(c[2] + "@example.com");
            u.setIdNumber(String.format("%013d", i + 1)); // obviously fake ID number
            u.setUsername(c[2]);
            u.setPassword(CUSTOMER_PASSWORD);
            u.setPhoneNumber(c[3]);
            u.setCarMake(c[4]);
            u.setCarModel(c[5]);
            u.setCarReg(c[6]);
            userDAO.createUser(u);
            userIds[i] = userDAO.getUserByUsername(c[2]).getId();
        }

        new AdminDAO().createAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, "0820000100");

        // Fuel requests spread over the last 60 days; the newest ones are still pending
        Random random = new Random(42);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO fuel_requests (user_id, customer_name, customer_phone, vehicle_model, registration, " +
                "fuel_type, fuel_amount, total_amount, status, request_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int requestCount = 28;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < requestCount; i++) {
                int userIndex = random.nextInt(customers.length);
                String[] c = customers[userIndex];
                boolean diesel = c[5].equals("Ranger") || c[5].equals("Hilux") || random.nextInt(5) == 0;
                double litres = 20 + random.nextInt(11) * 5; // 20 - 70 L
                double total = Math.round(litres * (diesel ? DIESEL_PRICE : PETROL_PRICE) * 100) / 100.0;

                int daysAgo = (requestCount - i) * 2 + random.nextInt(2);
                String status;
                if (i >= requestCount - 5) {
                    status = "pending";
                    daysAgo = requestCount - i - 1; // the last few days
                } else {
                    status = random.nextInt(6) == 0 ? "rejected" : "approved";
                }
                LocalDateTime when = now.minusDays(daysAgo).withHour(7 + random.nextInt(11)).withMinute(random.nextInt(60));

                pstmt.setInt(1, userIds[userIndex]);
                pstmt.setString(2, c[0] + " " + c[1]);
                pstmt.setString(3, c[3]);
                pstmt.setString(4, c[4] + " " + c[5]);
                pstmt.setString(5, c[6]);
                pstmt.setString(6, diesel ? "diesel" : "petrol");
                pstmt.setDouble(7, litres);
                pstmt.setDouble(8, total);
                pstmt.setString(9, status);
                pstmt.setString(10, when.format(fmt));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}

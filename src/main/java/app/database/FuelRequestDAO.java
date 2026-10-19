package app.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuelRequestDAO {

    public boolean createFuelRequest(String customerName, String customerPhone,
                                     String vehicleModel, String registration,
                                     String fuelType, double fuelAmount, double totalAmount,
                                     Integer userId) {  // ADD userId parameter
        String sql = "INSERT INTO fuel_requests (user_id, customer_name, customer_phone, vehicle_model, registration, fuel_type, fuel_amount, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, userId);  // Can be null for non-logged in users
            pstmt.setString(2, customerName);
            pstmt.setString(3, customerPhone);
            pstmt.setString(4, vehicleModel);
            pstmt.setString(5, registration);
            pstmt.setString(6, fuelType);
            pstmt.setDouble(7, fuelAmount);
            pstmt.setDouble(8, totalAmount);

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String[]> getPendingRequests() {
        List<String[]> requests = new ArrayList<>();
        String sql = "SELECT * FROM fuel_requests WHERE status = 'pending' ORDER BY request_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String[] request = {
                        String.valueOf(rs.getInt("id")),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getString("vehicle_model"),
                        rs.getString("registration"),
                        rs.getString("fuel_type"),
                        String.valueOf(rs.getDouble("fuel_amount")),
                        String.valueOf(rs.getDouble("total_amount")),
                        rs.getString("status"),
                        rs.getString("request_date")
                };
                requests.add(request);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return requests;
    }

    // ADD THESE MISSING METHODS:

    public List<String[]> getAllFuelRequests() {
        List<String[]> requests = new ArrayList<>();
        String sql = "SELECT * FROM fuel_requests ORDER BY request_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String[] request = {
                        String.valueOf(rs.getInt("id")),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getString("vehicle_model"),
                        rs.getString("registration"),
                        rs.getString("fuel_type"),
                        String.valueOf(rs.getDouble("fuel_amount")),
                        String.valueOf(rs.getDouble("total_amount")),
                        rs.getString("status"),
                        rs.getString("request_date")
                };
                requests.add(request);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return requests;
    }

    /** The newest requests made by one customer (same columns as getAllFuelRequests). */
    public List<String[]> getRequestsForUser(int userId, int limit) {
        List<String[]> requests = new ArrayList<>();
        String sql = "SELECT * FROM fuel_requests WHERE user_id = ? ORDER BY request_date DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getString("vehicle_model"),
                        rs.getString("registration"),
                        rs.getString("fuel_type"),
                        String.valueOf(rs.getDouble("fuel_amount")),
                        String.valueOf(rs.getDouble("total_amount")),
                        rs.getString("status"),
                        rs.getString("request_date")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return requests;
    }

    public boolean updateRequestStatus(int requestId, String status) {
        String sql = "UPDATE fuel_requests SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, requestId);

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getTotalMoneyPaid() {
        String sql = "SELECT SUM(total_amount) AS total FROM fuel_requests WHERE status = 'approved'";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public double getCustomerTotal(String customerName) {
        String sql = "SELECT SUM(total_amount) AS total FROM fuel_requests WHERE customer_name = ? AND status = 'approved'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public double calculateProfit() {
        // Simple profit calculation: 15% margin on approved requests
        String sql = "SELECT SUM(total_amount) AS revenue FROM fuel_requests WHERE status = 'approved'";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                double revenue = rs.getDouble("revenue");
                return revenue * 0.15; // 15% profit margin
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }
}
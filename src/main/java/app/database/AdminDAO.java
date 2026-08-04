package app.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDAO {

    public boolean authenticateAdmin(String username, String password) {
        String sql = "SELECT * FROM admin_users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ADD THIS MISSING METHOD:
    public String getAdminPhone() {
        String sql = "SELECT phone_number FROM admin_users LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("phone_number");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "+27123456789"; // Default fallback
    }
}
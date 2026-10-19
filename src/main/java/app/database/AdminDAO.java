package app.database;

import app.security.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminDAO {

    public boolean authenticateAdmin(String username, String password) {
        String sql = "SELECT password FROM admin_users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();
            return rs.next() && PasswordHasher.verify(password, rs.getString("password"));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasAnyAdmin() {
        String sql = "SELECT 1 FROM admin_users LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createAdmin(String username, String password, String phoneNumber) {
        String sql = "INSERT INTO admin_users (username, password, phone_number) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, PasswordHasher.hash(password));
            pstmt.setString(3, phoneNumber);
            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE admin_users SET password = ? WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, PasswordHasher.hash(newPassword));
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Returns the admin's phone number, or null if no admin (or no number) is set. */
    public String getAdminPhone() {
        String sql = "SELECT phone_number FROM admin_users WHERE phone_number <> '' LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("phone_number");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

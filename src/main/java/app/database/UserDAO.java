package app.database;

import app.models.User;
import app.security.PasswordHasher;
import java.sql.*;

public class UserDAO {

    /** Saves a new user; the plaintext password on the User is hashed before it is stored. */
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (first_name, last_name, email, id_number, username, password, phone_number, car_make, car_model, car_reg) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getIdNumber());
            pstmt.setString(5, user.getUsername());
            pstmt.setString(6, PasswordHasher.hash(user.getPassword()));
            pstmt.setString(7, user.getPhoneNumber());
            pstmt.setString(8, user.getCarMake());
            pstmt.setString(9, user.getCarModel());
            pstmt.setString(10, user.getCarReg());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            user.setPassword(null); // don't keep the plaintext password in memory
        }
    }

    /** Returns the user if the username and password match, otherwise null. */
    public User authenticate(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && PasswordHasher.verify(password, rs.getString("password"))) {
                return getUserByUsername(username);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /** Loads a user's profile. The password hash is never copied onto the User object. */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setIdNumber(rs.getString("id_number"));
                user.setUsername(rs.getString("username"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setCarMake(rs.getString("car_make"));
                user.setCarModel(rs.getString("car_model"));
                user.setCarReg(rs.getString("car_reg"));
                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean userExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}

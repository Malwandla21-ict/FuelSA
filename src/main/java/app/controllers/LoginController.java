package app.controllers;

import app.SceneController;
import app.database.UserDAO;
import app.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;

    @FXML
    private void onLogin() {
        // Add null check to prevent crash
        if (tfUsername == null || pfPassword == null) {
            System.out.println("❌ FXML fields not injected properly");
            return;
        }

        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.authenticate(username, password);

        if (user != null) {
            SceneController.show("request_fuel", user);
        } else {
            showAlert("Error", "Invalid username or password.");
        }
    }

    @FXML
    private void onRegister() {
        SceneController.show("register");
    }

    @FXML
    private void onForgot() {
        showAlert("Forgot Password", "Please contact admin to reset your password.");
    }

    @FXML
    private void onAdminLogin() {
        // Navigate to admin login - you already have this implemented
        SceneController.show("admin-login");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
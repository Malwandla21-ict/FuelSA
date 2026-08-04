package app.controllers;

import app.SceneController;
import app.database.AdminDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AdminLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private AdminDAO adminDAO = new AdminDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill all fields!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        boolean authenticated = adminDAO.authenticateAdmin(username, password);
        if (authenticated) {
            statusLabel.setText("Login successful!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            SceneController.show("admin-dashboard");
        } else {
            statusLabel.setText("Invalid admin credentials!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleBack() {
        SceneController.show("login");
    }
}
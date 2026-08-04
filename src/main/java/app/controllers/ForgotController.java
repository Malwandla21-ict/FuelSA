package app.controllers;

import app.SceneController;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class ForgotController {

    @FXML private TextField txtUsernameOrEmail;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Text txtMessage;

    @FXML
    private void onReset() {
        String userInput = txtUsernameOrEmail.getText().trim();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (userInput.isEmpty()) {
            txtMessage.setText("Please enter your username or email.");
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            txtMessage.setText("Please enter and confirm your new password.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            txtMessage.setText("Passwords do not match.");
            return;
        }

        if (newPassword.length() < 6) {
            txtMessage.setText("Password must be at least 6 characters.");
            return;
        }

        // Simulate reset success
        txtMessage.setStyle("-fx-fill: green;");
        txtMessage.setText("✅ Password reset successful (mock). You may now log in.");
    }

    @FXML
    private void onBack() {
        SceneController.show("login");
    }
}
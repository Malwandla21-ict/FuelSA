package app.controllers;

import app.SceneController;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import app.ui.Ui;
import app.ui.Ui.MessageType;

public class ForgotController {

    @FXML private TextField txtUsernameOrEmail;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMessage;

    @FXML
    private void onReset() {
        String userInput = txtUsernameOrEmail.getText().trim();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (userInput.isEmpty()) {
            Ui.showMessage(lblMessage, "Please enter your username or email.", MessageType.ERROR);
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Ui.showMessage(lblMessage, "Please enter and confirm your new password.", MessageType.ERROR);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Ui.showMessage(lblMessage, "Passwords do not match.", MessageType.ERROR);
            return;
        }

        if (newPassword.length() < 6) {
            Ui.showMessage(lblMessage, "Password must be at least 6 characters.", MessageType.ERROR);
            return;
        }

        // Real password reset is not implemented yet - never pretend it worked
        Ui.showMessage(lblMessage, "Password reset is not available yet. Please contact the FuelSA admin.", MessageType.INFO);
    }

    @FXML
    private void onBack() {
        SceneController.show("login");
    }
}
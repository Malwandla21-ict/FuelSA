package app.controllers;

import app.SceneController;
import app.database.AdminDAO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class AdminLoginController {

    // The old hardcoded default was published on GitHub, so it must never stay in use
    private static final String LEAKED_DEFAULT_PASSWORD = "admin123";
    private static final int MIN_ADMIN_PASSWORD_LENGTH = 8;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private AdminDAO adminDAO = new AdminDAO();

    @FXML
    private void initialize() {
        if (!adminDAO.hasAnyAdmin()) {
            statusLabel.setText("No admin account exists yet. Enter a username and click LOGIN to create one.");
            statusLabel.setStyle("-fx-text-fill: #2563eb;");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (!adminDAO.hasAnyAdmin()) {
            createFirstAdmin(username);
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill all fields!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        boolean authenticated = adminDAO.authenticateAdmin(username, password);
        if (authenticated) {
            if (LEAKED_DEFAULT_PASSWORD.equals(password) || password.length() < MIN_ADMIN_PASSWORD_LENGTH) {
                // Old/weak password: require a new one before entering the dashboard
                Optional<String[]> result = askForNewPassword(
                        "Change Admin Password",
                        "Your admin password is the old default or too weak.\nChoose a new password to continue.",
                        false);
                if (result.isEmpty() || !adminDAO.updatePassword(username, result.get()[0])) {
                    showError("Password not changed - login cancelled.");
                    return;
                }
            }
            statusLabel.setText("Login successful!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            SceneController.show("admin-dashboard");
        } else {
            showError("Invalid admin credentials!");
        }
    }

    private void createFirstAdmin(String username) {
        if (username.isEmpty()) {
            showError("Enter the username for the new admin account.");
            return;
        }

        Optional<String[]> result = askForNewPassword(
                "Create Admin Account",
                "Create the admin account \"" + username + "\".\nThe phone number receives new-request SMS alerts.",
                true);
        if (result.isEmpty()) {
            return;
        }

        if (adminDAO.createAdmin(username, result.get()[0], result.get()[1])) {
            statusLabel.setText("Admin account created!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            SceneController.show("admin-dashboard");
        } else {
            showError("Could not create the admin account.");
        }
    }

    /** Returns {password, phone} or empty if cancelled. Re-prompts until the input is valid. */
    private Optional<String[]> askForNewPassword(String title, String header, boolean askPhone) {
        PasswordField newPassword = new PasswordField();
        PasswordField confirm = new PasswordField();
        TextField phone = new TextField();
        phone.setPromptText("e.g. 0821234567");
        Label error = new Label();
        error.setStyle("-fx-text-fill: #e74c3c;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("New password:"), newPassword);
        grid.addRow(1, new Label("Confirm password:"), confirm);
        if (askPhone) {
            grid.addRow(2, new Label("Admin phone:"), phone);
        }
        grid.add(error, 0, 3, 2, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Keep the dialog open while the input is invalid
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String pw = newPassword.getText();
            String phoneDigits = phone.getText().replaceAll("[^0-9]", "");
            String problem = null;
            if (pw.length() < MIN_ADMIN_PASSWORD_LENGTH) {
                problem = "Password must be at least " + MIN_ADMIN_PASSWORD_LENGTH + " characters.";
            } else if (pw.equals(LEAKED_DEFAULT_PASSWORD)) {
                problem = "That password is not allowed.";
            } else if (!pw.equals(confirm.getText())) {
                problem = "Passwords do not match.";
            } else if (askPhone && phoneDigits.length() != 10) {
                problem = "Enter a 10-digit phone number.";
            }
            if (problem != null) {
                error.setText(problem);
                event.consume();
            }
        });

        Optional<ButtonType> clicked = dialog.showAndWait();
        if (clicked.isPresent() && clicked.get() == ButtonType.OK) {
            return Optional.of(new String[]{newPassword.getText(), phone.getText().replaceAll("[^0-9]", "")});
        }
        return Optional.empty();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    @FXML
    private void handleBack() {
        SceneController.show("login");
    }
}

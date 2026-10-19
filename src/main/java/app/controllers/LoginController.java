package app.controllers;

import app.SceneController;
import app.database.UserDAO;
import app.models.User;
import app.ui.Ui;
import app.ui.Ui.MessageType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Label lblMessage;

    @FXML
    private void onLogin() {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            Ui.showMessage(lblMessage, "Please enter both your username and password.", MessageType.ERROR);
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.authenticate(username, password);

        if (user != null) {
            SceneController.show("request_fuel", user);
        } else {
            pfPassword.clear();
            Ui.showMessage(lblMessage, "Incorrect username or password.", MessageType.ERROR);
        }
    }

    @FXML
    private void onRegister() {
        SceneController.show("register");
    }

    @FXML
    private void onForgot() {
        Ui.showMessage(lblMessage, "Please contact the FuelSA admin to reset your password.", MessageType.INFO);
    }

    @FXML
    private void onAdminLogin() {
        SceneController.show("admin-login");
    }
}

package app.controllers;

import app.SceneController;
import app.models.User;
import app.database.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import app.ui.Ui;
import app.ui.Ui.MessageType;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;

public class RegisterController {
    @FXML private TextField firstName, lastName, email, idNumber, username, carReg, phoneNumber;
    @FXML private PasswordField password, passwordConfirm;
    @FXML private ComboBox<String> cbCarMake, cbCarModel;
    @FXML private Label lblMessage;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        loadCarMakes();
        cbCarMake.setOnAction(e -> loadModelsForSelectedMake());

        // Add phone number validation listener
        phoneNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneNumber.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadCarMakes() {
        try {
            InputStream is = getClass().getResourceAsStream("/data/cars.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            NodeList makes = doc.getElementsByTagName("make");
            for (int i = 0; i < makes.getLength(); i++) {
                Element el = (Element) makes.item(i);
                cbCarMake.getItems().add(el.getAttribute("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadModelsForSelectedMake() {
        cbCarModel.getItems().clear();
        String make = cbCarMake.getValue();
        if (make == null) return;
        try {
            InputStream is = getClass().getResourceAsStream("/data/cars.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            NodeList makes = doc.getElementsByTagName("make");
            for (int i = 0; i < makes.getLength(); i++) {
                Element el = (Element) makes.item(i);
                if (make.equals(el.getAttribute("name"))) {
                    NodeList models = el.getElementsByTagName("model");
                    for (int j = 0; j < models.getLength(); j++) {
                        cbCarModel.getItems().add(models.item(j).getTextContent());
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onSubmit() {
        // Reset message
        Ui.hideMessage(lblMessage);

        // Validate all fields
        if (firstName.getText().isEmpty() || lastName.getText().isEmpty() ||
                email.getText().isEmpty() || idNumber.getText().isEmpty() ||
                username.getText().trim().isEmpty() || password.getText().isEmpty() ||
                phoneNumber.getText().isEmpty()) {
            Ui.showMessage(lblMessage, "Please fill in all required fields.", MessageType.ERROR);
            return;
        }

        if (!password.getText().equals(passwordConfirm.getText())) {
            Ui.showMessage(lblMessage, "Passwords do not match.", MessageType.ERROR);
            return;
        }

        // Validate password strength
        if (password.getText().length() < 6) {
            Ui.showMessage(lblMessage, "Password must be at least 6 characters long.", MessageType.ERROR);
            return;
        }

        // Check if username already exists
        if (userDAO.userExists(username.getText().trim())) {
            Ui.showMessage(lblMessage, "Username already exists.", MessageType.ERROR);
            return;
        }

        // Validate phone number format and length
        String phone = phoneNumber.getText().replaceAll("[^0-9]", "");
        if (phone.length() != 10) {
            Ui.showMessage(lblMessage, "Please enter a valid 10-digit South African phone number (e.g., 0812345678).", MessageType.ERROR);
            return;
        }

        // Validate ID number (South African ID numbers are 13 digits)
        String idNum = idNumber.getText().replaceAll("[^0-9]", "");
        if (idNum.length() != 13) {
            Ui.showMessage(lblMessage, "Please enter a valid 13-digit South African ID number.", MessageType.ERROR);
            return;
        }

        // Validate email format
        if (!email.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            Ui.showMessage(lblMessage, "Please enter a valid email address.", MessageType.ERROR);
            return;
        }

        User u = new User();
        u.setFirstName(firstName.getText().trim());
        u.setLastName(lastName.getText().trim());
        u.setEmail(email.getText().trim());
        u.setIdNumber(idNumber.getText().trim());
        u.setUsername(username.getText().trim());
        u.setPassword(password.getText());
        u.setPhoneNumber(phoneNumber.getText().trim()); // Store the original format
        u.setCarMake(cbCarMake.getValue());
        u.setCarModel(cbCarModel.getValue());
        u.setCarReg(carReg.getText().trim());

        // Save to database
        boolean saved = userDAO.createUser(u);
        if (saved) {
            Ui.showMessage(lblMessage, "Account created! Signing you in...", MessageType.SUCCESS);

            // Auto-login after successful registration
            User newUser = userDAO.getUserByUsername(username.getText().trim());
            if (newUser != null) {
                // Pass the new user to the request fuel controller
                SceneController.show("request_fuel", newUser);
            } else {
                Ui.showMessage(lblMessage, "Registration successful but login failed. Please login manually.", MessageType.ERROR);
            }

        } else {
            Ui.showMessage(lblMessage, "Registration failed. Please try again.", MessageType.ERROR);
        }
    }

    @FXML
    private void onBack() {
        SceneController.show("login");
    }
}
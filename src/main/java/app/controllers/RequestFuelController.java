package app.controllers;

import app.SceneController;
import app.database.DemoData;
import app.database.FuelRequestDAO;
import app.models.User;
import app.services.SMSService;
import app.ui.Ui;
import app.ui.Ui.MessageType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;
import java.util.prefs.Preferences;

public class RequestFuelController {

    @FXML private ComboBox<String> cbMake, cbModel;
    @FXML private TextField tfCarName, tfFuelCapacity;
    @FXML private Hyperlink btnRefreshPrices;
    @FXML private Label lblInitials, lblUserName, lblGreeting, lblRegistration;
    @FXML private Label lblPricePerLitre, lblLitres, lblEstimate, lblFeedback;
    @FXML private VBox recentList;

    private static final double PRICE_PER_LITER = 20.15;
    private static final int RECENT_LIMIT = 5;

    // Saved car details are stored per user so one person never sees another's car
    private Preferences prefs = Preferences.userNodeForPackage(RequestFuelController.class).node("guest");
    private User currentUser;

    @FXML
    private void initialize() {
        // Get the logged-in user from SceneController
        Object userData = SceneController.getData("request_fuel");
        if (userData instanceof User) {
            this.currentUser = (User) userData;
            // Demo users get their own namespace so they never share saved cars with real user ids
            String prefix = DemoData.isActive() ? "demo-user-" : "user-";
            prefs = Preferences.userNodeForPackage(RequestFuelController.class).node(prefix + currentUser.getId());
        }
        showUser();

        loadCarMakes();
        cbMake.setOnAction(e -> {
            savePref("carMake", cbMake.getValue());
            loadModels();
        });
        cbModel.setOnAction(e -> savePref("carModel", cbModel.getValue()));
        tfCarName.textProperty().addListener((obs, oldVal, newVal) -> savePref("carName", newVal));
        tfFuelCapacity.textProperty().addListener((obs, oldVal, newVal) -> {
            savePref("fuelCapacity", newVal);
            updateEstimate();
        });
        btnRefreshPrices.setOnAction(e -> refreshFuelPrices());

        loadSavedCarInfo();
        loadCustomCars();

        lblPricePerLitre.setText(Ui.money(PRICE_PER_LITER));
        updateEstimate();
        loadRecentRequests();
    }

    private void showUser() {
        if (currentUser == null) {
            lblUserName.setText("Guest");
            lblInitials.setText("?");
            lblRegistration.setVisible(false);
            return;
        }
        lblUserName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        lblInitials.setText(Ui.initials(currentUser.getFirstName(), currentUser.getLastName()));
        lblGreeting.setText("Hi " + currentUser.getFirstName() + ", tell us about your car and how much fuel you need.");

        String reg = currentUser.getCarReg();
        lblRegistration.setText(reg == null || reg.isBlank() ? "No registration on file" : reg);
    }

    private void loadCarMakes() {
        try {
            InputStream is = getClass().getResourceAsStream("/data/cars.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            var doc = db.parse(is);
            var makes = doc.getElementsByTagName("make");
            for (int i = 0; i < makes.getLength(); i++) {
                var el = (org.w3c.dom.Element) makes.item(i);
                cbMake.getItems().add(el.getAttribute("name"));
            }
        } catch (Exception ex) {
            Ui.showMessage(lblFeedback, "Could not load car makes: " + ex.getMessage(), MessageType.ERROR);
        }
    }

    private void loadModels() {
        cbModel.getItems().clear();
        String make = cbMake.getValue();
        if (make == null) return;

        try {
            InputStream is = getClass().getResourceAsStream("/data/cars.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            var doc = db.parse(is);
            var makes = doc.getElementsByTagName("make");
            for (int i = 0; i < makes.getLength(); i++) {
                var el = (org.w3c.dom.Element) makes.item(i);
                if (make.equals(el.getAttribute("name"))) {
                    var models = el.getElementsByTagName("model");
                    for (int j = 0; j < models.getLength(); j++) {
                        cbModel.getItems().add(models.item(j).getTextContent());
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            Ui.showMessage(lblFeedback, "Could not load models: " + ex.getMessage(), MessageType.ERROR);
        }
    }

    // Load custom cars from preferences
    private void loadCustomCars() {
        String customCars = prefs.get("customCars", "");
        if (!customCars.isEmpty()) {
            for (String car : customCars.split(";;")) {
                if (!car.trim().isEmpty() && !cbModel.getItems().contains(car.trim())) {
                    cbModel.getItems().add(car.trim());
                }
            }
        }
    }

    // Save custom cars to preferences
    private void saveCustomCar(String carName) {
        if (carName == null || carName.trim().isEmpty()) return;

        String customCars = prefs.get("customCars", "");
        StringBuilder updatedCars = new StringBuilder();
        if (!customCars.isEmpty()) {
            updatedCars.append(customCars).append(";;");
        }
        updatedCars.append(carName.trim());
        prefs.put("customCars", updatedCars.toString());
    }

    // Preferences.put throws on null, which happens whenever a ComboBox is cleared
    private void savePref(String key, String value) {
        if (value == null || value.isEmpty()) {
            prefs.remove(key);
        } else {
            prefs.put(key, value);
        }
    }

    private void loadSavedCarInfo() {
        String savedCar = prefs.get("carName", "");
        String savedFuelCap = prefs.get("fuelCapacity", "");
        String savedMake = prefs.get("carMake", "");
        String savedModel = prefs.get("carModel", "");

        // First visit: start from the car on the user's profile
        if (savedMake.isEmpty() && currentUser != null && currentUser.getCarMake() != null) {
            savedMake = currentUser.getCarMake();
            savedModel = currentUser.getCarModel() == null ? "" : currentUser.getCarModel();
        }

        if (!savedCar.isEmpty()) tfCarName.setText(savedCar);
        if (!savedFuelCap.isEmpty()) tfFuelCapacity.setText(savedFuelCap);
        if (!savedMake.isEmpty()) {
            cbMake.setValue(savedMake);
            loadModels();
        }
        if (!savedModel.isEmpty()) cbModel.setValue(savedModel);
    }

    /** Parses the litres field; returns -1 when it is empty or not a positive number. */
    private double parseLitres() {
        try {
            double litres = Double.parseDouble(tfFuelCapacity.getText().trim().replace(',', '.'));
            return litres > 0 ? litres : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updateEstimate() {
        double litres = parseLitres();
        if (litres > 0) {
            lblLitres.setText(Ui.litres(litres));
            lblEstimate.setText(Ui.money(litres * PRICE_PER_LITER));
        } else {
            lblLitres.setText("—");
            lblEstimate.setText(Ui.money(0));
        }
    }

    private void refreshFuelPrices() {
        lblPricePerLitre.setText(Ui.money(PRICE_PER_LITER));
        Ui.showMessage(lblFeedback, "Prices are up to date: Petrol 95 " + Ui.money(PRICE_PER_LITER) + "/L (sample prices).", MessageType.INFO);
    }

    @FXML
    private void onRequestFuel() {
        if (currentUser == null) {
            Ui.showMessage(lblFeedback, "You are not signed in. Please sign in first.", MessageType.ERROR);
            return;
        }

        String make = cbMake.getValue();
        String model = cbModel.getValue();
        String customCar = tfCarName.getText().trim();

        // Either a dropdown selection OR a custom car is enough
        boolean hasDropdownSelection = (make != null && model != null && !make.isEmpty() && !model.isEmpty());
        boolean hasCustomCar = !customCar.isEmpty();

        if (!hasDropdownSelection && !hasCustomCar) {
            Ui.showMessage(lblFeedback, "Choose your car's make and model, or type your car in the box.", MessageType.ERROR);
            return;
        }

        double fuelCap = parseLitres();
        if (fuelCap <= 0) {
            Ui.showMessage(lblFeedback, "Enter how many litres you need (a number above 0).", MessageType.ERROR);
            return;
        }

        double totalPrice = fuelCap * PRICE_PER_LITER;

        String customerName = currentUser.getFirstName() + " " + currentUser.getLastName();
        String customerPhone = currentUser.getPhoneNumber();

        // Registration is the user's number plate from their profile, not the car model
        String registration = currentUser.getCarReg();
        if (registration == null || registration.isBlank()) {
            registration = "Not provided";
        }

        String vehicleDescription;
        if (hasDropdownSelection) {
            vehicleDescription = make + " " + model;
        } else {
            vehicleDescription = customCar;
            saveCustomCar(customCar);
        }

        FuelRequestDAO fuelDAO = new FuelRequestDAO();
        SMSService smsService = new SMSService();

        boolean saved = fuelDAO.createFuelRequest(
                customerName,
                customerPhone,
                vehicleDescription,
                registration,
                "petrol",
                fuelCap,
                totalPrice,
                currentUser.getId()
        );

        if (saved) {
            smsService.sendAdminAlert(customerName, vehicleDescription, registration, "petrol", fuelCap, totalPrice);
            smsService.sendCustomerConfirmation(customerPhone, currentUser.getFirstName(), "submitted");

            Ui.showMessage(lblFeedback, "Request sent! " + Ui.litres(fuelCap) + " for your " + vehicleDescription
                    + " (" + Ui.money(totalPrice) + "). We'll SMS you once it's reviewed.", MessageType.SUCCESS);
            loadRecentRequests();
        } else {
            Ui.showMessage(lblFeedback, "Something went wrong submitting your request. Please try again.", MessageType.ERROR);
        }
    }

    private void loadRecentRequests() {
        recentList.getChildren().clear();
        if (currentUser == null) {
            recentList.getChildren().add(Ui.emptyState("mdi2c-clipboard-text-outline", "Sign in to see your requests."));
            return;
        }

        List<String[]> requests = new FuelRequestDAO().getRequestsForUser(currentUser.getId(), RECENT_LIMIT);
        if (requests.isEmpty()) {
            recentList.getChildren().add(Ui.emptyState("mdi2c-clipboard-text-outline", "No requests yet. Your first one will show up here."));
            return;
        }

        for (int i = 0; i < requests.size(); i++) {
            String[] r = requests.get(i);
            HBox row = requestRow(r);
            if (i == requests.size() - 1) row.getStyleClass().add("last");
            recentList.getChildren().add(row);
        }
    }

    // Columns: 3 vehicle, 6 litres, 7 total, 8 status, 9 date
    private HBox requestRow(String[] r) {
        StackPane icon = new StackPane(new FontIcon("mdi2g-gas-station-outline"));
        icon.getStyleClass().add("row-icon");

        Label title = new Label(Ui.litres(r[6]) + " · " + r[3]);
        title.getStyleClass().add("row-title");
        Label sub = new Label(Ui.date(r[9]));
        sub.getStyleClass().add("row-sub");
        VBox text = new VBox(2, title, sub);
        text.setMinWidth(0);
        HBox.setHgrow(text, javafx.scene.layout.Priority.ALWAYS);

        Label amount = new Label(Ui.money(r[7]));
        amount.getStyleClass().add("row-amount");
        VBox right = new VBox(4, amount, Ui.badge(r[8]));
        right.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        right.setMinWidth(Region.USE_PREF_SIZE);

        HBox row = new HBox(icon, text, right);
        row.getStyleClass().add("list-row");
        return row;
    }

    @FXML
    private void onAddCar() {
        String newCar = tfCarName.getText().trim();
        if (!newCar.isEmpty()) {
            if (!cbModel.getItems().contains(newCar)) {
                cbModel.getItems().add(newCar);
                saveCustomCar(newCar);
            }

            cbModel.setValue(newCar);
            tfCarName.clear();
            Ui.showMessage(lblFeedback, "Added \"" + newCar + "\" to your cars.", MessageType.SUCCESS);
            savePref("carModel", newCar);
        } else {
            Ui.showMessage(lblFeedback, "Type a car name first, then click Add.", MessageType.ERROR);
        }
    }

    @FXML
    private void onClearCarInfo() {
        cbMake.getSelectionModel().clearSelection();
        cbModel.getItems().clear();
        tfCarName.clear();
        tfFuelCapacity.clear();

        // Reload default models after clearing
        loadModels();
        loadCustomCars();

        prefs.remove("carMake");
        prefs.remove("carModel");
        prefs.remove("carName");
        prefs.remove("fuelCapacity");

        Ui.showMessage(lblFeedback, "Car details cleared.", MessageType.INFO);
    }

    @FXML
    private void onLogout() {
        currentUser = null;
        SceneController.clearData("request_fuel");
        SceneController.show("login");
    }

    // Method to set current user (called from other controllers)
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}

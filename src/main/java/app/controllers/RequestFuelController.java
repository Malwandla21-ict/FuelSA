package app.controllers;

import app.SceneController;
import app.database.FuelRequestDAO;
import app.models.User;
import app.services.SMSService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.prefs.Preferences;

public class RequestFuelController {

    @FXML private ComboBox<String> cbMake, cbModel;
    @FXML private TextField tfCarName, tfFuelCapacity, tfFuelAmount;
    @FXML private TextArea taDetails;
    @FXML private Button btnRefreshPrices;
    @FXML private Text txtPriceInfo, txtTotalCost;

    private final double PRICE_PER_LITER = 20.15;
    private final Preferences prefs = Preferences.userNodeForPackage(RequestFuelController.class);
    private User currentUser;

    @FXML
    private void initialize() {
        // Get the logged-in user from SceneController
        Object userData = SceneController.getData("request_fuel");
        if (userData instanceof User) {
            this.currentUser = (User) userData;
            System.out.println("User logged in: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        }

        // SAFE INITIALIZATION - check if components exist first
        if (cbMake != null) {
            loadCarMakes();
            cbMake.setOnAction(e -> {
                prefs.put("carMake", cbMake.getValue());
                loadModels();
            });
        }

        if (cbModel != null) {
            cbModel.setOnAction(e -> prefs.put("carModel", cbModel.getValue()));
        }

        if (tfCarName != null) {
            tfCarName.textProperty().addListener((obs, oldVal, newVal) -> prefs.put("carName", newVal));
        }

        if (tfFuelCapacity != null) {
            tfFuelCapacity.textProperty().addListener((obs, oldVal, newVal) -> prefs.put("fuelCapacity", newVal));
        }

        if (btnRefreshPrices != null) {
            btnRefreshPrices.setOnAction(e -> refreshFuelPrices());
        }

        loadSavedCarInfo();
        loadCustomCars(); // Load previously saved custom cars

        if (txtPriceInfo != null) {
            txtPriceInfo.setText("Current price: R" + PRICE_PER_LITER + " per liter");
        }
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
            if (taDetails != null) {
                taDetails.setText("Error loading car makes: " + ex.getMessage());
            }
        }
    }

    private void loadModels() {
        if (cbModel == null) return;

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
            if (taDetails != null) {
                taDetails.setText("Error loading models: " + ex.getMessage());
            }
        }
    }

    // NEW METHOD: Load custom cars from preferences
    private void loadCustomCars() {
        if (cbModel == null) return;

        // Load custom cars from preferences
        String customCars = prefs.get("customCars", "");
        if (!customCars.isEmpty()) {
            String[] cars = customCars.split(";;");
            for (String car : cars) {
                if (!car.trim().isEmpty() && !cbModel.getItems().contains(car.trim())) {
                    cbModel.getItems().add(car.trim());
                }
            }
        }
    }

    // NEW METHOD: Save custom cars to preferences
    private void saveCustomCar(String carName) {
        if (carName == null || carName.trim().isEmpty()) return;

        // Get existing custom cars
        String customCars = prefs.get("customCars", "");
        StringBuilder updatedCars = new StringBuilder();

        if (!customCars.isEmpty()) {
            updatedCars.append(customCars).append(";;");
        }
        updatedCars.append(carName.trim());

        // Save back to preferences
        prefs.put("customCars", updatedCars.toString());
    }

    private void loadSavedCarInfo() {
        String savedCar = prefs.get("carName", "");
        String savedFuelCap = prefs.get("fuelCapacity", "");
        String savedMake = prefs.get("carMake", "");
        String savedModel = prefs.get("carModel", "");

        if (tfCarName != null && !savedCar.isEmpty()) tfCarName.setText(savedCar);
        if (tfFuelCapacity != null && !savedFuelCap.isEmpty()) tfFuelCapacity.setText(savedFuelCap);
        if (cbMake != null && !savedMake.isEmpty()) cbMake.setValue(savedMake);
        if (cbModel != null && !savedModel.isEmpty()) {
            loadModels();
            cbModel.setValue(savedModel);
        }
    }

    @FXML
    private void refreshFuelPrices() {
        String info = "Latest (mock) prices:\nPetrol 95: R20.15 / L\nDiesel 0.05%: R19.10 / L\n(For live prices embed AA script or call an API.)";
        if (txtPriceInfo != null) {
            txtPriceInfo.setText(info);
        }
    }

    @FXML
    private void onRequestFuel() {
        if (cbMake == null || cbModel == null || tfCarName == null || tfFuelCapacity == null || taDetails == null) {
            System.out.println("UI components not loaded properly");
            return;
        }

        // Check if user is logged in
        if (currentUser == null) {
            taDetails.setText("Error: No user logged in. Please login first.");
            return;
        }

        String make = cbMake.getValue();
        String model = cbModel.getValue();
        String customCar = tfCarName.getText().trim();
        String fuelCapStr = tfFuelCapacity.getText();

        // FIXED LOGIC: Either dropdown selection OR custom car, not both required
        boolean hasDropdownSelection = (make != null && model != null && !make.isEmpty() && !model.isEmpty());
        boolean hasCustomCar = !customCar.isEmpty();

        if (!hasDropdownSelection && !hasCustomCar) {
            taDetails.setText("Please either select a car from the dropdown OR enter your car name.");
            return;
        }

        if (fuelCapStr.isEmpty()) {
            taDetails.setText("Please enter fuel capacity.");
            return;
        }

        double fuelCap;
        try {
            fuelCap = Double.parseDouble(fuelCapStr);
            if (fuelCap <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            taDetails.setText("Fuel capacity must be a positive number.");
            return;
        }

        double totalPrice = fuelCap * PRICE_PER_LITER;

        // Use ACTUAL user data
        String customerName = currentUser.getFirstName() + " " + currentUser.getLastName();
        String customerPhone = currentUser.getPhoneNumber();

        // Determine vehicle description
        String vehicleDescription;
        String registration;

        if (hasDropdownSelection) {
            vehicleDescription = make + " " + model;
            registration = model;
        } else {
            vehicleDescription = customCar;
            registration = customCar;

            // If using custom car, save it for future use
            saveCustomCar(customCar);
        }

        // Save to database and send SMS
        FuelRequestDAO fuelDAO = new FuelRequestDAO();
        SMSService smsService = new SMSService();

        // Use ACTUAL user data instead of hardcoded values
        boolean saved = fuelDAO.createFuelRequest(
                customerName,       // ACTUAL customer name
                customerPhone,      // ACTUAL customer phone
                vehicleDescription, // Either dropdown selection OR custom car
                registration,       // Use model for dropdown, custom car for manual entry
                "petrol",
                fuelCap,
                totalPrice,
                currentUser.getId()  // ACTUAL user ID
        );

        if (saved) {
            // Send SMS to admin with ACTUAL customer info
            smsService.sendAdminAlert(
                    customerName,      // ACTUAL customer name
                    vehicleDescription,
                    registration,
                    "petrol",
                    fuelCap,
                    totalPrice
            );

            // Send SMS to ACTUAL customer
            smsService.sendCustomerConfirmation(
                    customerPhone,        // ACTUAL customer phone
                    currentUser.getFirstName(), // ACTUAL first name
                    "submitted"           // status
            );

            String output = String.format(
                    "✅ Fuel request submitted!\nCar: %s\nCapacity: %.2f L\nEstimated Cost: R%.2f\n\nSMS sent to you and admin for approval.",
                    vehicleDescription, fuelCap, totalPrice
            );
            taDetails.setText(output);
        } else {
            taDetails.setText("Error submitting request. Please try again.");
        }
    }

    @FXML
    private void onAddCar() {
        if (tfCarName == null || cbModel == null || taDetails == null) return;

        String newCar = tfCarName.getText().trim();
        if (!newCar.isEmpty()) {
            // Add to dropdown if not already there
            if (!cbModel.getItems().contains(newCar)) {
                cbModel.getItems().add(newCar);
                // Save to persistent storage
                saveCustomCar(newCar);
            }

            // Select the newly added car
            cbModel.setValue(newCar);
            tfCarName.clear();
            taDetails.setText("✅ Car added to your list: " + newCar);
            prefs.put("carModel", newCar);
        } else {
            taDetails.setText("⚠ Enter a car name to add.");
        }
    }

    @FXML
    private void onClearCarInfo() {
        if (cbMake != null) cbMake.getSelectionModel().clearSelection();
        if (cbModel != null) cbModel.getItems().clear();
        if (tfCarName != null) tfCarName.clear();
        if (tfFuelCapacity != null) tfFuelCapacity.clear();

        // Reload default models after clearing
        loadModels();
        loadCustomCars();

        prefs.remove("carMake");
        prefs.remove("carModel");
        prefs.remove("carName");
        prefs.remove("fuelCapacity");

        if (taDetails != null) {
            taDetails.setText("🗑️ Car information cleared.");
        }
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
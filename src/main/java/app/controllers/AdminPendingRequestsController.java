package app.controllers;

import app.SceneController;
import app.database.FuelRequestDAO;
import app.services.SMSService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminPendingRequestsController implements Initializable {

    @FXML private TableView<String[]> requestsTable;
    @FXML private TableColumn<String[], String> idColumn;
    @FXML private TableColumn<String[], String> customerColumn;
    @FXML private TableColumn<String[], String> vehicleColumn;
    @FXML private TableColumn<String[], String> fuelColumn;
    @FXML private TableColumn<String[], String> amountColumn;
    @FXML private TableColumn<String[], String> totalColumn;
    @FXML private TableColumn<String[], String> dateColumn;

    private FuelRequestDAO fuelDAO = new FuelRequestDAO();
    private SMSService smsService = new SMSService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadPendingRequests();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        customerColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        vehicleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3] + " (" + data.getValue()[4] + ")"));
        fuelColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[5]));
        amountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[6] + "L"));
        totalColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("R" + data.getValue()[7]));
        dateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[9]));
    }

    private void loadPendingRequests() {
        List<String[]> requests = fuelDAO.getPendingRequests();
        requestsTable.getItems().setAll(requests);
    }

    @FXML
    private void handleRefresh() {
        loadPendingRequests();
    }

    @FXML
    private void handleApprove() {
        String[] selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int requestId = Integer.parseInt(selected[0]);

            // DEBUG LOGGING
            System.out.println("🔍 DEBUG - Approval details:");
            System.out.println("   Request ID: " + selected[0]);
            System.out.println("   Customer Name: " + selected[1]);
            System.out.println("   Customer Phone: " + selected[2]);
            System.out.println("   Vehicle: " + selected[3]);
            System.out.println("   Registration: " + selected[4]);

            boolean success = fuelDAO.updateRequestStatus(requestId, "approved");
            if (success) {
                // Send SMS to customer
                String customerPhone = selected[2]; // phone is at index 2
                String customerName = selected[1]; // name is at index 1

                System.out.println("📱 Sending approval SMS to: " + customerPhone + " (" + customerName + ")");

                smsService.sendCustomerConfirmation(customerPhone, customerName, "approved");
                showAlert("Success", "Request approved successfully!");
                loadPendingRequests();
            } else {
                showAlert("Error", "Failed to approve request!");
            }
        } else {
            showAlert("Warning", "Please select a request to approve!");
        }
    }

    @FXML
    private void handleReject() {
        String[] selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Rejection");
            confirmAlert.setHeaderText("Reject Fuel Request");
            confirmAlert.setContentText("Are you sure you want to reject this fuel request?");

            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                int requestId = Integer.parseInt(selected[0]);

                // DEBUG LOGGING
                System.out.println("🔍 DEBUG - Rejection details:");
                System.out.println("   Request ID: " + selected[0]);
                System.out.println("   Customer Name: " + selected[1]);
                System.out.println("   Customer Phone: " + selected[2]);

                boolean success = fuelDAO.updateRequestStatus(requestId, "rejected");
                if (success) {
                    // Send SMS to customer
                    String customerPhone = selected[2]; // phone is at index 2
                    String customerName = selected[1]; // name is at index 1

                    System.out.println("📱 Sending rejection SMS to: " + customerPhone + " (" + customerName + ")");

                    smsService.sendCustomerConfirmation(customerPhone, customerName, "rejected");
                    showAlert("Success", "Request rejected successfully!");
                    loadPendingRequests();
                } else {
                    showAlert("Error", "Failed to reject request!");
                }
            }
        } else {
            showAlert("Warning", "Please select a request to reject!");
        }
    }

    @FXML
    private void handleBack() {
        SceneController.show("admin-dashboard");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package app.controllers;

import app.SceneController;
import app.database.FuelRequestDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminAnalyticsController implements Initializable {

    @FXML private Label totalMoneyLabel;
    @FXML private Label customerTotalLabel;
    @FXML private Label profitLabel;
    @FXML private Label totalRequestsLabel;
    @FXML private Label approvedRequestsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private TextField customerNameField;

    private FuelRequestDAO fuelDAO = new FuelRequestDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAnalytics();
    }

    private void loadAnalytics() {
        // 1.5.3 - Total money paid for fuel
        double totalMoney = fuelDAO.getTotalMoneyPaid();
        totalMoneyLabel.setText(String.format("R%.2f", totalMoney));

        // 1.5.5 - Profit calculation
        double profit = fuelDAO.calculateProfit();
        profitLabel.setText(String.format("R%.2f", profit));

        // Additional analytics
        loadRequestStats();
    }

    private void loadRequestStats() {
        List<String[]> allRequests = fuelDAO.getAllFuelRequests();

        int total = allRequests.size();
        int approved = 0;
        int pending = 0;

        // FIXED: Use traditional loop instead of streams
        for (String[] request : allRequests) {
            String status = request[8]; // status is at index 8
            if ("approved".equals(status)) {
                approved++;
            } else if ("pending".equals(status)) {
                pending++;
            }
        }

        totalRequestsLabel.setText(String.valueOf(total));
        approvedRequestsLabel.setText(String.valueOf(approved));
        pendingRequestsLabel.setText(String.valueOf(pending));
    }

    @FXML
    private void handleCalculateCustomerTotal() {
        String customerName = customerNameField.getText().trim();
        if (!customerName.isEmpty()) {
            double customerTotal = fuelDAO.getCustomerTotal(customerName);
            customerTotalLabel.setText(String.format("R%.2f", customerTotal));
        } else {
            customerTotalLabel.setText("Enter customer name");
        }
    }

    @FXML
    private void handleRefresh() {
        loadAnalytics();
        customerNameField.clear();
        customerTotalLabel.setText("R0.00");
    }

    @FXML
    private void handleBack() {
        SceneController.show("admin-dashboard");
    }
}
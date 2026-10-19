package app.controllers;

import app.SceneController;
import app.database.FuelRequestDAO;
import app.database.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label totalUsersLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label totalPurchasesLabel;
    @FXML private Label profitLabel;

    private FuelRequestDAO fuelDAO = new FuelRequestDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Update pending requests count
        int pendingCount = fuelDAO.getPendingRequests().size();
        pendingRequestsLabel.setText(String.valueOf(pendingCount));

        // Update total purchases - FIXED METHOD NAME
        double totalMoney = fuelDAO.getTotalMoneyPaid();
        totalPurchasesLabel.setText(String.format("R%.2f", totalMoney));

        // Update profit - FIXED METHOD NAME
        double profit = fuelDAO.calculateProfit();
        profitLabel.setText(String.format("R%.2f", profit));

        // Total registered customers
        totalUsersLabel.setText(String.valueOf(new UserDAO().countUsers()));
    }

    @FXML
    private void handlePendingRequests() {
        SceneController.show("admin-pending-requests");
    }

    @FXML
    private void handleRequestHistory() {
        SceneController.show("admin-request-history");
    }

    @FXML
    private void handleAnalytics() {
        SceneController.show("admin-analytics");
    }

    @FXML
    private void handleLogout() {
        SceneController.show("login");
    }
}
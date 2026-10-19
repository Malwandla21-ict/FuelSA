package app.controllers;

import app.database.FuelRequestDAO;
import app.services.SMSService;
import app.ui.Ui;
import app.ui.Ui.MessageType;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminPendingRequestsController implements Initializable {

    @FXML private AdminSidebarController sidebarController;
    @FXML private TableView<String[]> requestsTable;
    @FXML private TableColumn<String[], String> idColumn;
    @FXML private TableColumn<String[], String> customerColumn;
    @FXML private TableColumn<String[], String> vehicleColumn;
    @FXML private TableColumn<String[], String> fuelColumn;
    @FXML private TableColumn<String[], String> amountColumn;
    @FXML private TableColumn<String[], String> totalColumn;
    @FXML private TableColumn<String[], String> dateColumn;
    @FXML private Button btnApprove, btnReject;
    @FXML private Label lblSubtitle, lblFeedback;

    private FuelRequestDAO fuelDAO = new FuelRequestDAO();
    private SMSService smsService = new SMSService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarController.setActive("pending");
        setupTableColumns();
        requestsTable.setPlaceholder(Ui.emptyState("mdi2c-check-all", "All caught up. There are no pending requests."));

        // Approve / reject only make sense with a row selected
        btnApprove.disableProperty().bind(requestsTable.getSelectionModel().selectedItemProperty().isNull());
        btnReject.disableProperty().bind(requestsTable.getSelectionModel().selectedItemProperty().isNull());

        loadPendingRequests();
    }

    // Columns: 0 id, 1 customer, 2 phone, 3 vehicle, 4 registration, 5 fuel, 6 litres, 7 total, 8 status, 9 date
    private void setupTableColumns() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        customerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        vehicleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3] + " (" + data.getValue()[4] + ")"));
        fuelColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.capitalize(data.getValue()[5])));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.litres(data.getValue()[6])));
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.money(data.getValue()[7])));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.date(data.getValue()[9])));
    }

    private void loadPendingRequests() {
        List<String[]> requests = fuelDAO.getPendingRequests();
        requestsTable.getItems().setAll(requests);
        lblSubtitle.setText(requests.isEmpty()
                ? "Nothing waiting for review."
                : requests.size() + " waiting for review. Select one, then approve or reject it.");
        sidebarController.refreshPendingCount();
    }

    @FXML
    private void handleRefresh() {
        Ui.hideMessage(lblFeedback);
        loadPendingRequests();
    }

    @FXML
    private void handleApprove() {
        String[] selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Ui.showMessage(lblFeedback, "Select a request to approve.", MessageType.ERROR);
            return;
        }

        int requestId = Integer.parseInt(selected[0]);
        if (fuelDAO.updateRequestStatus(requestId, "approved")) {
            smsService.sendCustomerConfirmation(selected[2], selected[1], "approved");
            Ui.showMessage(lblFeedback, "Approved request #" + requestId + " for " + selected[1]
                    + " (" + Ui.money(selected[7]) + "). The customer will get an SMS.", MessageType.SUCCESS);
            loadPendingRequests();
        } else {
            Ui.showMessage(lblFeedback, "Could not approve request #" + requestId + ". Please try again.", MessageType.ERROR);
        }
    }

    @FXML
    private void handleReject() {
        String[] selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Ui.showMessage(lblFeedback, "Select a request to reject.", MessageType.ERROR);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reject request");
        confirmAlert.setHeaderText("Reject request #" + selected[0] + " from " + selected[1] + "?");
        confirmAlert.setContentText(Ui.litres(selected[6]) + " for " + selected[3] + " (" + Ui.money(selected[7])
                + "). The customer will be notified by SMS.");
        confirmAlert.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        int requestId = Integer.parseInt(selected[0]);
        if (fuelDAO.updateRequestStatus(requestId, "rejected")) {
            smsService.sendCustomerConfirmation(selected[2], selected[1], "rejected");
            Ui.showMessage(lblFeedback, "Rejected request #" + requestId + " from " + selected[1] + ".", MessageType.INFO);
            loadPendingRequests();
        } else {
            Ui.showMessage(lblFeedback, "Could not reject request #" + requestId + ". Please try again.", MessageType.ERROR);
        }
    }
}

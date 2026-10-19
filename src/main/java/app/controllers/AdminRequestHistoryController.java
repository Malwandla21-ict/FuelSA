package app.controllers;

import app.database.FuelRequestDAO;
import app.ui.Ui;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminRequestHistoryController implements Initializable {

    private static final String ALL_STATUSES = "All statuses";

    @FXML private AdminSidebarController sidebarController;
    @FXML private TableView<String[]> requestsTable;
    @FXML private TableColumn<String[], String> idColumn;
    @FXML private TableColumn<String[], String> customerColumn;
    @FXML private TableColumn<String[], String> phoneColumn;
    @FXML private TableColumn<String[], String> vehicleColumn;
    @FXML private TableColumn<String[], String> fuelColumn;
    @FXML private TableColumn<String[], String> amountColumn;
    @FXML private TableColumn<String[], String> totalColumn;
    @FXML private TableColumn<String[], String> statusColumn;
    @FXML private TableColumn<String[], String> dateColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblSubtitle;

    private FuelRequestDAO fuelDAO = new FuelRequestDAO();
    private List<String[]> allRequests;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarController.setActive("history");
        setupTableColumns();
        requestsTable.setPlaceholder(Ui.emptyState("mdi2m-magnify-close", "No requests match your search."));

        cbStatus.getItems().setAll(ALL_STATUSES, "Pending", "Approved", "Rejected");
        cbStatus.setValue(ALL_STATUSES);
        cbStatus.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        loadAllRequests();
    }

    // Columns: 0 id, 1 customer, 2 phone, 3 vehicle, 4 registration, 5 fuel, 6 litres, 7 total, 8 status, 9 date
    private void setupTableColumns() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        customerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        vehicleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3] + " (" + data.getValue()[4] + ")"));
        fuelColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.capitalize(data.getValue()[5])));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.litres(data.getValue()[6])));
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.money(data.getValue()[7])));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[8]));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.date(data.getValue()[9])));

        // Status shown as a coloured pill
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(empty || item == null ? null : Ui.badge(item));
            }
        });
    }

    private void loadAllRequests() {
        allRequests = fuelDAO.getAllFuelRequests();
        applyFilters();
    }

    private void applyFilters() {
        String term = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String status = cbStatus.getValue() == null || ALL_STATUSES.equals(cbStatus.getValue())
                ? null : cbStatus.getValue().toLowerCase();

        List<String[]> filtered = allRequests.stream()
                .filter(r -> status == null || status.equals(r[8]))
                .filter(r -> term.isEmpty()
                        || r[1].toLowerCase().contains(term)   // customer name
                        || r[3].toLowerCase().contains(term)   // vehicle model
                        || r[4].toLowerCase().contains(term))  // registration
                .toList();
        requestsTable.getItems().setAll(filtered);

        lblSubtitle.setText(filtered.size() == allRequests.size()
                ? allRequests.size() + " requests, newest first."
                : "Showing " + filtered.size() + " of " + allRequests.size() + " requests.");
    }

    @FXML
    private void handleRefresh() {
        loadAllRequests();
        sidebarController.refreshPendingCount();
    }
}

package app.controllers;

import app.SceneController;
import app.database.FuelRequestDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminRequestHistoryController implements Initializable {

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

    private FuelRequestDAO fuelDAO = new FuelRequestDAO();
    private List<String[]> allRequests;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadAllRequests();

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRequests(newValue);
        });
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        customerColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        phoneColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        vehicleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3] + " (" + data.getValue()[4] + ")"));
        fuelColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[5]));
        amountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[6] + "L"));
        totalColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("R" + data.getValue()[7]));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[8]));
        dateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[9]));

        // Style status column based on status
        statusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<String[], String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "approved":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "rejected":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "pending":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void loadAllRequests() {
        allRequests = fuelDAO.getAllFuelRequests();
        requestsTable.getItems().setAll(allRequests);
    }

    private void filterRequests(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            requestsTable.getItems().setAll(allRequests);
        } else {
            List<String[]> filtered = allRequests.stream()
                    .filter(request ->
                            request[1].toLowerCase().contains(searchTerm.toLowerCase()) || // customer name
                                    request[3].toLowerCase().contains(searchTerm.toLowerCase()) || // vehicle model
                                    request[4].toLowerCase().contains(searchTerm.toLowerCase()) || // registration
                                    request[8].toLowerCase().contains(searchTerm.toLowerCase())    // status
                    )
                    .toList();
            requestsTable.getItems().setAll(filtered);
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllRequests();
        searchField.clear();
    }

    @FXML
    private void handleBack() {
        SceneController.show("admin-dashboard");
    }
}
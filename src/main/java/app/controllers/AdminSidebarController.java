package app.controllers;

import app.SceneController;
import app.database.FuelRequestDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/** Controller for admin-sidebar.fxml. Each admin page calls setActive(...) with its own key. */
public class AdminSidebarController {

    @FXML private Button navDashboard, navPending, navHistory, navAnalytics;
    @FXML private Label pendingCount;

    @FXML
    private void initialize() {
        // Let the graphic stretch so the count pill reaches the right edge
        ((HBox) navPending.getGraphic()).prefWidthProperty().bind(navPending.widthProperty().subtract(24));
        refreshPendingCount();
    }

    public void refreshPendingCount() {
        int pending = new FuelRequestDAO().getPendingRequests().size();
        pendingCount.setText(String.valueOf(pending));
        pendingCount.setVisible(pending > 0);
    }

    /** key: dashboard | pending | history | analytics */
    public void setActive(String key) {
        Button active = switch (key) {
            case "pending" -> navPending;
            case "history" -> navHistory;
            case "analytics" -> navAnalytics;
            default -> navDashboard;
        };
        for (Button b : new Button[]{navDashboard, navPending, navHistory, navAnalytics}) {
            b.getStyleClass().remove("active");
        }
        active.getStyleClass().add("active");
    }

    @FXML private void goDashboard() { SceneController.show("admin-dashboard"); }
    @FXML private void goPending() { SceneController.show("admin-pending-requests"); }
    @FXML private void goHistory() { SceneController.show("admin-request-history"); }
    @FXML private void goAnalytics() { SceneController.show("admin-analytics"); }
    @FXML private void logout() { SceneController.show("login"); }
}

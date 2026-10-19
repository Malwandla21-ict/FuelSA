package app.controllers;

import app.SceneController;
import app.database.FuelRequestDAO;
import app.database.UserDAO;
import app.ui.Ui;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private AdminSidebarController sidebarController;
    @FXML private Label totalUsersLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label totalPurchasesLabel;
    @FXML private Label profitLabel;
    @FXML private Button btnReviewPending;
    @FXML private VBox recentList;
    @FXML private VBox statusBox;

    private static final int RECENT_LIMIT = 6;

    private FuelRequestDAO fuelDAO = new FuelRequestDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarController.setActive("dashboard");
        loadDashboardData();
    }

    private void loadDashboardData() {
        int pendingCount = fuelDAO.getPendingRequests().size();
        pendingRequestsLabel.setText(String.valueOf(pendingCount));
        btnReviewPending.setText(pendingCount > 0 ? "Review pending (" + pendingCount + ")" : "Review pending");

        totalPurchasesLabel.setText(Ui.money(fuelDAO.getTotalMoneyPaid()));
        profitLabel.setText(Ui.money(fuelDAO.calculateProfit()));

        // Total registered customers
        totalUsersLabel.setText(String.valueOf(new UserDAO().countUsers()));

        List<String[]> all = fuelDAO.getAllFuelRequests();
        loadRecent(all);
        loadStatusBreakdown(all);
    }

    // Columns: 1 customer, 3 vehicle, 6 litres, 7 total, 8 status, 9 date
    private void loadRecent(List<String[]> all) {
        recentList.getChildren().clear();
        if (all.isEmpty()) {
            recentList.getChildren().add(Ui.emptyState("mdi2c-clipboard-text-outline", "No fuel requests yet."));
            return;
        }
        int count = Math.min(RECENT_LIMIT, all.size());
        for (int i = 0; i < count; i++) {
            String[] r = all.get(i);

            StackPane avatar = new StackPane(new Label(initialsOf(r[1])));
            avatar.getStyleClass().add("avatar");

            Label title = new Label(r[1]);
            title.getStyleClass().add("row-title");
            Label sub = new Label(Ui.litres(r[6]) + " · " + r[3] + " · " + Ui.date(r[9]));
            sub.getStyleClass().add("row-sub");
            VBox text = new VBox(2, title, sub);
            text.setMinWidth(0);
            HBox.setHgrow(text, Priority.ALWAYS);

            Label amount = new Label(Ui.money(r[7]));
            amount.getStyleClass().add("row-amount");
            amount.setMinWidth(Region.USE_PREF_SIZE);
            Label badge = Ui.badge(r[8]);
            badge.setMinWidth(76);
            badge.setAlignment(Pos.CENTER);

            HBox row = new HBox(avatar, text, amount, badge);
            row.getStyleClass().add("list-row");
            if (i == count - 1) row.getStyleClass().add("last");
            recentList.getChildren().add(row);
        }
    }

    private void loadStatusBreakdown(List<String[]> all) {
        statusBox.getChildren().clear();
        int total = Math.max(all.size(), 1);
        String[][] statuses = {{"approved", "Approved"}, {"pending", "Pending"}, {"rejected", "Rejected"}};
        for (String[] s : statuses) {
            long n = all.stream().filter(r -> s[0].equals(r[8])).count();
            double share = (double) n / total;

            Label name = new Label(s[1]);
            name.getStyleClass().add("row-title");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label value = new Label(n + "  ·  " + Math.round(share * 100) + "%");
            value.getStyleClass().add("row-sub");

            Region fill = new Region();
            fill.getStyleClass().addAll("bar-fill", s[0]);
            Region track = new Region();
            track.getStyleClass().add("bar-track");
            StackPane bar = new StackPane(track, fill);
            bar.setAlignment(Pos.CENTER_LEFT);
            fill.maxWidthProperty().bind(bar.widthProperty().multiply(share));
            track.setMaxWidth(Double.MAX_VALUE);

            statusBox.getChildren().add(new VBox(8, new HBox(name, spacer, value), bar));
        }

        Label caption = new Label(all.size() + " requests in total");
        caption.getStyleClass().add("stat-caption");
        statusBox.getChildren().add(caption);
    }

    private static String initialsOf(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        return Ui.initials(parts[0], parts.length > 1 ? parts[parts.length - 1] : "");
    }

    @FXML
    private void handlePendingRequests() {
        SceneController.show("admin-pending-requests");
    }

    @FXML
    private void handleRequestHistory() {
        SceneController.show("admin-request-history");
    }
}

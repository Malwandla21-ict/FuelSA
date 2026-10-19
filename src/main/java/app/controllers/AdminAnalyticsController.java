package app.controllers;

import app.database.FuelRequestDAO;
import app.ui.Ui;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminAnalyticsController implements Initializable {

    private static final int WEEKS = 8;
    private static final int TOP_CUSTOMERS = 5;
    private static final DateTimeFormatter WEEK_LABEL = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    @FXML private AdminSidebarController sidebarController;
    @FXML private Label totalMoneyLabel;
    @FXML private Label customerTotalLabel;
    @FXML private Label lblCustomerCaption;
    @FXML private Label profitLabel;
    @FXML private Label totalRequestsLabel;
    @FXML private Label approvedRequestsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label lblChartCaption;
    @FXML private TextField customerNameField;
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private VBox topCustomers;

    private FuelRequestDAO fuelDAO = new FuelRequestDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sidebarController.setActive("analytics");

        // Rand values on the axis, e.g. R2k
        ((NumberAxis) revenueChart.getYAxis()).setTickLabelFormatter(new StringConverter<>() {
            @Override public String toString(Number n) {
                double v = n.doubleValue();
                return v >= 1000 ? "R" + Math.round(v / 100) / 10.0 + "k" : "R" + Math.round(v);
            }
            @Override public Number fromString(String s) { return 0; }
        });

        loadAnalytics();
    }

    private void loadAnalytics() {
        // 1.5.3 - Total money paid for fuel
        totalMoneyLabel.setText(Ui.money(fuelDAO.getTotalMoneyPaid()));

        // 1.5.5 - Profit calculation
        profitLabel.setText(Ui.money(fuelDAO.calculateProfit()));

        List<String[]> allRequests = fuelDAO.getAllFuelRequests();
        loadRequestStats(allRequests);
        loadWeeklyRevenue(allRequests);
        loadTopCustomers(allRequests);
    }

    // Columns: 1 customer, 7 total, 8 status, 9 date
    private void loadRequestStats(List<String[]> allRequests) {
        int approved = 0;
        int pending = 0;
        for (String[] request : allRequests) {
            if ("approved".equals(request[8])) {
                approved++;
            } else if ("pending".equals(request[8])) {
                pending++;
            }
        }

        totalRequestsLabel.setText(String.valueOf(allRequests.size()));
        approvedRequestsLabel.setText(approved + " approved");
        pendingRequestsLabel.setText(pending + " pending");
    }

    private void loadWeeklyRevenue(List<String[]> allRequests) {
        LocalDate thisWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate firstWeek = thisWeek.minusWeeks(WEEKS - 1);

        Map<LocalDate, Double> totals = new LinkedHashMap<>();
        for (int i = 0; i < WEEKS; i++) {
            totals.put(firstWeek.plusWeeks(i), 0.0);
        }
        for (String[] r : allRequests) {
            if (!"approved".equals(r[8]) || r[9] == null || r[9].length() < 10) continue;
            LocalDate week = LocalDate.parse(r[9].substring(0, 10)).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            if (totals.containsKey(week)) {
                totals.merge(week, Double.parseDouble(r[7]), Double::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        totals.forEach((week, total) -> series.getData().add(new XYChart.Data<>(week.format(WEEK_LABEL), total)));
        revenueChart.getData().setAll(List.of(series));
        lblChartCaption.setText("Weeks starting Monday · last " + WEEKS + " weeks");

        // Hover tooltip on every bar (bar nodes exist once the series is added)
        for (XYChart.Data<String, Number> d : series.getData()) {
            Tooltip tip = new Tooltip("Week of " + d.getXValue() + "\n" + Ui.money(d.getYValue().doubleValue()));
            tip.setShowDelay(Duration.millis(80));
            Tooltip.install(d.getNode(), tip);
        }
    }

    private void loadTopCustomers(List<String[]> allRequests) {
        topCustomers.getChildren().clear();
        Map<String, Double> byCustomer = allRequests.stream()
                .filter(r -> "approved".equals(r[8]))
                .collect(Collectors.groupingBy(r -> r[1], Collectors.summingDouble(r -> Double.parseDouble(r[7]))));

        if (byCustomer.isEmpty()) {
            topCustomers.getChildren().add(Ui.emptyState("mdi2a-account-outline", "No approved purchases yet."));
            return;
        }

        double max = byCustomer.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        byCustomer.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(TOP_CUSTOMERS)
                .forEach(e -> {
                    Label name = new Label(e.getKey());
                    name.getStyleClass().add("row-title");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    Label value = new Label(Ui.money(e.getValue()));
                    value.getStyleClass().add("row-sub");

                    Region fill = new Region();
                    fill.getStyleClass().addAll("bar-fill", "magnitude");
                    Region track = new Region();
                    track.getStyleClass().add("bar-track");
                    track.setMaxWidth(Double.MAX_VALUE);
                    StackPane bar = new StackPane(track, fill);
                    bar.setAlignment(Pos.CENTER_LEFT);
                    fill.maxWidthProperty().bind(bar.widthProperty().multiply(e.getValue() / max));

                    topCustomers.getChildren().add(new VBox(6, new HBox(name, spacer, value), bar));
                });
    }

    @FXML
    private void handleCalculateCustomerTotal() {
        String customerName = customerNameField.getText().trim();
        if (customerName.isEmpty()) {
            lblCustomerCaption.setText("Enter a customer's full name");
            customerTotalLabel.setText(Ui.money(0));
            return;
        }
        double customerTotal = fuelDAO.getCustomerTotal(customerName);
        lblCustomerCaption.setText(customerTotal > 0
                ? "Total spent by " + customerName
                : "No approved purchases for \"" + customerName + "\"");
        customerTotalLabel.setText(Ui.money(customerTotal));
    }

    @FXML
    private void handleRefresh() {
        loadAnalytics();
        customerNameField.clear();
        lblCustomerCaption.setText("Total spent");
        customerTotalLabel.setText(Ui.money(0));
        sidebarController.refreshPendingCount();
    }
}

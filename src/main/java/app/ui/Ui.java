package app.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Shared formatting and small UI building blocks so every screen looks the same. */
public final class Ui {
    private static final DecimalFormat MONEY = new DecimalFormat("R#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat LITRES = new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DateTimeFormatter DB_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SHOW_DATE = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);

    public enum MessageType { ERROR, SUCCESS, INFO }

    private Ui() {}

    public static String money(double amount) {
        return MONEY.format(amount);
    }

    public static String money(String amount) {
        try {
            return money(Double.parseDouble(amount));
        } catch (NumberFormatException | NullPointerException e) {
            return amount;
        }
    }

    public static String litres(double amount) {
        return LITRES.format(amount) + " L";
    }

    public static String litres(String amount) {
        try {
            return litres(Double.parseDouble(amount));
        } catch (NumberFormatException | NullPointerException e) {
            return amount;
        }
    }

    /** "2026-10-19 10:22:00" -> "19 Oct 2026, 10:22" */
    public static String date(String dbDate) {
        if (dbDate == null) return "";
        try {
            return LocalDateTime.parse(dbDate, DB_DATE).format(SHOW_DATE);
        } catch (DateTimeParseException e) {
            return dbDate;
        }
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    /** Coloured pill for pending / approved / rejected. */
    public static Label badge(String status) {
        String key = status == null ? "" : status.toLowerCase();
        Label badge = new Label(capitalize(key));
        badge.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        badge.getStyleClass().add("badge");
        badge.getStyleClass().add(switch (key) {
            case "pending", "approved", "rejected" -> key;
            default -> "neutral";
        });
        return badge;
    }

    public static String initials(String firstName, String lastName) {
        String a = firstName == null || firstName.isBlank() ? "" : firstName.substring(0, 1);
        String b = lastName == null || lastName.isBlank() ? "" : lastName.substring(0, 1);
        return (a + b).toUpperCase();
    }

    /** Shows an inline message (error / success / info) in a Label styled as a banner. */
    public static void showMessage(Label label, String text, MessageType type) {
        label.getStyleClass().removeAll("message", "error", "success", "info");
        label.getStyleClass().addAll("message", type.name().toLowerCase());
        String icon = switch (type) {
            case ERROR -> "mdi2a-alert-circle-outline";
            case SUCCESS -> "mdi2c-check-circle-outline";
            case INFO -> "mdi2i-information-outline";
        };
        label.setGraphic(new FontIcon(icon));
        label.setText(text);
        label.setWrapText(true);
        label.setVisible(true);
        label.setManaged(true);
    }

    public static void hideMessage(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    public static VBox emptyState(String iconLiteral, String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        VBox box = new VBox(new FontIcon(iconLiteral), label);
        box.getStyleClass().add("empty-state");
        box.setAlignment(Pos.CENTER);
        return box;
    }
}

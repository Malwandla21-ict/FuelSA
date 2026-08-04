package app.services;

import app.database.AdminDAO;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SMSService {
    private AdminDAO adminDAO = new AdminDAO();
    private static final String BULKSMS_URL = "https://api.bulksms.com/v1/messages";
    private static final String TOKEN_ID = "REDACTED";
    private static final String TOKEN_SECRET = "REDACTED";
    private static final String SENDER_ID = "FuelSA";

    /**
     * Sends SMS alert to admin about new fuel request
     */
    public void sendAdminAlert(String customerName, String vehicle, String registration,
                               String fuelType, double amount, double total) {
        String adminPhone = adminDAO.getAdminPhone();
        String formattedPhone = formatPhoneNumber(adminPhone);

        String message = String.format(
                "FuelSA Alert: New request from %s - %s (%s). %s: %.1fL. Total: R%.2f. Please check dashboard.",
                customerName, vehicle, registration, fuelType, amount, total
        );

        sendMessage("Admin", formattedPhone, message);
    }

    /**
     * Sends SMS confirmation to customer about their request status
     */
    public void sendCustomerConfirmation(String customerPhone, String customerName, String status) {
        String formattedPhone = formatPhoneNumber(customerPhone);

        String message = formatCustomerMessage(customerName, status);
        sendMessage("Customer", formattedPhone, message);
    }

    /**
     * Formats customer message based on status type
     */
    private String formatCustomerMessage(String customerName, String status) {
        switch (status.toLowerCase()) {
            case "approved":
                return String.format(
                        "Hi %s, great news! Your fuel request has been APPROVED ✅. " +
                                "Your fuel will be delivered shortly. Thank you for choosing FuelSA!",
                        customerName
                );

            case "rejected":
                return String.format(
                        "Hi %s, we regret to inform you that your fuel request has been REJECTED ❌. " +
                                "Please contact support for more information. - FuelSA",
                        customerName
                );

            case "submitted":
            default:
                return String.format(
                        "Hi %s, your fuel request has been SUBMITTED. " +
                                " Thank you for using FuelSA!",
                        customerName
                );
        }
    }

    /**
     * Formats phone number for SMS delivery (converts 0xxx to 27xxx)
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            System.err.println("❌ Invalid phone number: null or empty");
            return "";
        }

        String digitsOnly = phone.replaceAll("[^0-9]", "");

        // Convert 0xxxxxxxxx to 27xxxxxxxxx
        if (digitsOnly.startsWith("0") && digitsOnly.length() == 10) {
            digitsOnly = "27" + digitsOnly.substring(1);
        }

        // Validate final format
        if (!digitsOnly.startsWith("27") || digitsOnly.length() != 11) {
            System.err.println("❌ Invalid phone format after processing: " + digitsOnly);
            return "";
        }

        return digitsOnly;
    }

    /**
     * Core method to send SMS message
     */
    private void sendMessage(String recipientType, String toNumber, String message) {
        if (toNumber.isEmpty()) {
            System.err.println("❌ Cannot send SMS - invalid phone number for " + recipientType);
            return;
        }

        System.out.println("📱 Sending SMS to " + recipientType + " (" + toNumber + "): " + message);

        try {
            boolean sent = sendSMSRequest(toNumber, message);

            if (sent) {
                System.out.println("✅ SMS delivered successfully to " + recipientType);
            } else {
                System.out.println("❌ Failed to send SMS to " + recipientType);
            }

        } catch (Exception e) {
            System.err.println("❌ Error sending SMS to " + recipientType + ": " + e.getMessage());
        }
    }

    /**
     * Makes actual HTTP request to BulkSMS API
     */
    private boolean sendSMSRequest(String toNumber, String message) {
        try {
            URL url = new URL(BULKSMS_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setup authentication and headers
            String credentials = TOKEN_ID + ":" + TOKEN_SECRET;
            String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + basicAuth);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10 second timeout
            connection.setReadTimeout(10000);

            // Create JSON payload
            String jsonPayload = String.format(
                    "{\"to\": \"%s\", \"body\": \"%s\", \"from\": \"%s\"}",
                    toNumber, message, SENDER_ID
            );

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Check response
            int responseCode = connection.getResponseCode();

            if (responseCode == 201) {
                return true;
            } else {
                logErrorDetails(connection, responseCode);
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Network error sending SMS: " + e.getMessage());
            return false;
        }
    }

    /**
     * Logs detailed error information from SMS API
     */
    private void logErrorDetails(HttpURLConnection connection, int responseCode) {
        System.err.println("❌ SMS API returned error code: " + responseCode);

        try (java.io.InputStream errorStream = connection.getErrorStream()) {
            if (errorStream != null) {
                String errorResponse = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                System.err.println("Error details: " + errorResponse);
            }
        } catch (Exception e) {
            System.err.println("Could not read error response: " + e.getMessage());
        }
    }
}
package app;

import app.database.DatabaseManager;
import app.database.DemoData;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // "--demo" uses a separate database filled with sample data (see DemoData)
            if (getParameters().getRaw().contains("--demo")) {
                DemoData.enable();
            }

            // Create tables and upgrade any old plaintext passwords before any screen loads
            DatabaseManager.init();

            primaryStage.setTitle(DemoData.isActive() ? "FuelSA (demo data)" : "FuelSA");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(780);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(700);

            SceneController.setStage(primaryStage);
            SceneController.show("login");

        } catch (Exception e) {
            System.err.println("❌ Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("🚀 Starting Fuel App...");
        launch(args);
    }
}

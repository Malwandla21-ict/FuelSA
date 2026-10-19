package app;

import app.database.DatabaseManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create tables and upgrade any old plaintext passwords before any screen loads
            DatabaseManager.init();

            // Set the stage in SceneController
            SceneController.setStage(primaryStage);

            // Show the login screen
            SceneController.show("login");

            // Set window title
            primaryStage.setTitle("Fuel App");
            primaryStage.setResizable(false);

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
package app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneController {
    private static Stage stage;
    private static Map<String, Object> controllers = new HashMap<>();
    private static Map<String, Object> data = new HashMap<>();

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void show(String fxml) {
        show(fxml, null);
    }

    public static void show(String fxml, Object controllerData) {
        try {
            // Store data to pass to controller
            if (controllerData != null) {
                data.put(fxml, controllerData);
            }

            // Use the correct path: /fxml/ instead of /app/views/
            String fxmlPath = "/fxml/" + fxml + ".fxml";
            System.out.println("Loading FXML: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(SceneController.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Store controller reference
            controllers.put(fxml, loader.getController());

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Error loading FXML: " + e.getMessage());
            e.printStackTrace();

            // Show error dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("FXML Error");
            alert.setHeaderText("Cannot load interface");
            alert.setContentText("Error loading " + fxml + ".fxml: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static Object getController(String fxml) {
        return controllers.get(fxml);
    }

    public static Object getData(String fxml) {
        return data.get(fxml);
    }

    public static void clearData(String fxml) {
        data.remove(fxml);
    }
}
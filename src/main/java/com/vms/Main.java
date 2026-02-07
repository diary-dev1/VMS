package com.vms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Charger le FXML du login (page de connexion)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vms/login.fxml"));
        Parent root = loader.load();

        // Créer la scène
        Scene scene = new Scene(root, 1200, 700);

        // Charger le CSS
        scene.getStylesheets().add(getClass().getResource("/com/vms/styles.css").toExternalForm());

        // Configurer le stage
        primaryStage.setTitle("VMS - Voucher Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    /**
     * Méthode pour changer de vue
     */
    public static void changeScene(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/vms/" + fxml));
        Parent root = loader.load();
        primaryStage.getScene().setRoot(root);
    }

    /**
     * Récupérer le stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

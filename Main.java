package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Chargement de la vue principale JavaFX depuis les ressources.
        URL fxmlUrl = Main.class.getResource("/ui/MainView.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("Fichier FXML introuvable: /ui/MainView.fxml");
        }

        // FXMLLoader instancie automatiquement le contrôleur défini dans le FXML.
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root);

        primaryStage.setTitle("Projet Velibs");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

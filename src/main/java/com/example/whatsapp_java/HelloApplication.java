package com.example.whatsapp_java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        // Charger le CSS global
        String css = HelloApplication.class.getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("Messagerie");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}

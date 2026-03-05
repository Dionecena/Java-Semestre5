package com.example.whatsapp_java.client;

import com.example.whatsapp_java.HelloApplication;
import com.example.whatsapp_java.utils.PasswordUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    @FXML
    public void initialize() {
        // Touche Entrée sur le champ mot de passe → connexion
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onLogin();
            }
        });
        // Touche Entrée sur le champ username → passer au mot de passe
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });
    }

    @FXML
    protected void onRegister() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠ Champs vides");
            return;
        }

        statusLabel.setText("Inscription en cours...");
        try {
            ChatClient client = new ChatClient(HOST, PORT);
            client.connect(new LoginListener());
            String hash = PasswordUtil.sha256(password);
            client.sendLine("REGISTER|" + username + "|" + hash);
            AppSession.setClient(client);
        } catch (Exception e) {
            statusLabel.setText("⚠ Impossible de joindre le serveur");
        }
    }

    @FXML
    protected void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠ Champs vides");
            return;
        }

        statusLabel.setText("Connexion en cours...");
        try {
            ChatClient client = new ChatClient(HOST, PORT);
            client.connect(new LoginListener());
            String hash = PasswordUtil.sha256(password);
            client.sendLine("LOGIN|" + username + "|" + hash);
            AppSession.setClient(client);
            AppSession.setUsername(username);
        } catch (Exception e) {
            statusLabel.setText("⚠ Impossible de joindre le serveur");
        }
    }

    private class LoginListener implements ChatClient.Listener {
        @Override
        public void onLineReceived(String line) {
            Platform.runLater(() -> handleServerLine(line));
        }

        @Override
        public void onDisconnected(Exception e) {
            Platform.runLater(() -> statusLabel.setText("⚠ Déconnecté du serveur"));
        }
    }

    private void handleServerLine(String line) {
        if (line == null) return;

        if (line.startsWith("OK|REGISTER")) {
            statusLabel.setText("✓ Inscription réussie. Vous pouvez vous connecter.");
            ChatClient c = AppSession.getClient();
            if (c != null) {
                new Thread(() -> {
                    try { Thread.sleep(200); } catch (Exception ignored) {}
                    c.close();
                    AppSession.setClient(null);
                }).start();
            }
            return;
        }

        if (line.startsWith("OK|LOGIN")) {
            statusLabel.setText("✓ Connexion réussie");
            openChatWindow();
            return;
        }

        if (line.startsWith("ERROR|")) {
            statusLabel.setText("⚠ " + line.substring("ERROR|".length()));
        }
    }

    private void openChatWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("chat-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("styles.css").toExternalForm());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Messagerie - " + AppSession.getUsername());
            stage.setScene(scene);
        } catch (Exception e) {
            statusLabel.setText("⚠ Erreur chargement interface: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.example.whatsapp_java.client;

import com.example.whatsapp_java.HelloApplication;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML private ListView<String>      usersList;
    @FXML private Label                 chatWithLabel;
    @FXML private ListView<MessageItem> messagesListView;
    @FXML private TextField             messageField;
    @FXML private Label                 statusLabel;

    private String currentChatUser;

    /** Liste observable des messages affichés dans la conversation courante */
    private final ObservableList<MessageItem> messages = FXCollections.observableArrayList();

    private boolean inHistory = false;
    private final List<MessageItem> historyBuffer = new ArrayList<>();

    @FXML
    public void initialize() {
        ChatClient client = AppSession.getClient();
        if (client == null) {
            statusLabel.setText("Client non initialisé");
            return;
        }

        // Lier la ListView à la liste observable
        messagesListView.setItems(messages);
        // Utiliser notre cellule personnalisée (bulles)
        messagesListView.setCellFactory(lv -> new MessageCell());
        // Fond transparent de la ListView
        messagesListView.setStyle("-fx-background-color: #0B141A; -fx-border-color: transparent;");

        client.setListener(new ChatListener());

        // Sélection d'un utilisateur → charger l'historique
        usersList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.isBlank()) {
                String name = extractUsername(newV);
                currentChatUser = name;
                chatWithLabel.setText("Conversation avec: " + currentChatUser);
                messages.clear();
                requestHistory();
            }
        });

        // Touche Entrée pour envoyer
        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onSend();
            }
        });

        // Demander la liste complète dès l'ouverture
        client.sendLine("ALL_USERS|");
    }

    /** Extrait le nom d'utilisateur depuis "username [ONLINE]" ou "username [OFFLINE]" */
    private String extractUsername(String item) {
        if (item == null) return "";
        int idx = item.lastIndexOf(" [");
        if (idx > 0) return item.substring(0, idx);
        return item;
    }

    @FXML
    protected void onRefreshUsers() {
        ChatClient client = AppSession.getClient();
        if (client != null) {
            client.sendLine("ALL_USERS|");
        }
    }

    @FXML
    protected void onSend() {
        if (currentChatUser == null || currentChatUser.isBlank()) {
            statusLabel.setText("Sélectionnez un utilisateur");
            return;
        }

        String msg = messageField.getText();
        if (msg == null) msg = "";
        msg = msg.trim();

        if (msg.isEmpty()) {
            statusLabel.setText("Message vide");
            return;
        }
        if (msg.length() > 1000) {
            statusLabel.setText("Message trop long (max 1000 caractères)");
            return;
        }

        ChatClient client = AppSession.getClient();
        if (client != null) {
            client.sendLine("SEND|" + currentChatUser + "|" + msg);
            addMessage(msg, true, nowTime());
            messageField.clear();
            statusLabel.setText("");
        }
    }

    @FXML
    protected void onLogout() {
        ChatClient client = AppSession.getClient();
        if (client != null) {
            client.sendLine("LOGOUT|");
            client.close();
            AppSession.setClient(null);
        }
        returnToLogin();
    }

    private void requestHistory() {
        if (currentChatUser == null) return;
        ChatClient client = AppSession.getClient();
        if (client != null) {
            client.sendLine("HISTORY|" + currentChatUser);
        }
    }

    // RG10 – retour à l'écran de login en cas de perte de connexion
    private void handleDisconnect(Exception e) {
        AppSession.setClient(null);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Connexion perdue");
        alert.setHeaderText("Déconnecté du serveur");
        alert.setContentText("La connexion au serveur a été perdue.\nVous allez être redirigé vers l'écran de connexion.");
        alert.showAndWait();
        returnToLogin();
    }

    private void returnToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("styles.css").toExternalForm());
            Stage stage = null;
            if (messagesListView != null && messagesListView.getScene() != null) {
                stage = (Stage) messagesListView.getScene().getWindow();
            } else if (usersList != null && usersList.getScene() != null) {
                stage = (Stage) usersList.getScene().getWindow();
            }
            if (stage != null) {
                stage.setTitle("Messagerie - Connexion");
                stage.setResizable(false);
                stage.setScene(scene);
            } else {
                Platform.exit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.exit();
        }
    }

    private class ChatListener implements ChatClient.Listener {
        @Override
        public void onLineReceived(String line) {
            Platform.runLater(() -> handleServerLine(line));
        }

        @Override
        public void onDisconnected(Exception e) {
            Platform.runLater(() -> handleDisconnect(e));
        }
    }

    private void handleServerLine(String line) {
        if (line == null) return;

        // Liste complète (online + offline)
        if (line.startsWith("ALL_USERS|")) {
            String csv = line.substring("ALL_USERS|".length());
            String selected = currentChatUser;
            usersList.getItems().clear();
            if (!csv.isBlank()) {
                for (String entry : csv.split(",")) {
                    if (entry.isBlank()) continue;
                    String[] kv = entry.split(":", 2);
                    String uname = kv[0];
                    String status = kv.length > 1 ? kv[1] : "OFFLINE";
                    if (!uname.equals(AppSession.getUsername())) {
                        usersList.getItems().add(uname + " [" + status + "]");
                    }
                }
            }
            // Restaurer la sélection si possible
            if (selected != null) {
                for (String item : usersList.getItems()) {
                    if (extractUsername(item).equals(selected)) {
                        usersList.getSelectionModel().select(item);
                        break;
                    }
                }
            }
            return;
        }

        // Liste online seulement (compatibilité)
        if (line.startsWith("USERS|")) {
            String csv = line.substring("USERS|".length());
            if (usersList.getItems().isEmpty() && !csv.isBlank()) {
                for (String u : csv.split(",")) {
                    if (!u.isBlank() && !u.equals(AppSession.getUsername())) {
                        usersList.getItems().add(u + " [ONLINE]");
                    }
                }
            }
            return;
        }

        if (line.startsWith("MSG|")) {
            String[] p = line.split("\\|", 4);
            if (p.length >= 3) {
                String from = p[1];
                String content = p[2];
                String ts = p.length >= 4 ? formatTimestamp(p[3]) : nowTime();
                if (currentChatUser != null && currentChatUser.equals(from)) {
                    // Message reçu dans la conversation ouverte
                    addMessage(content, false, ts);
                } else {
                    statusLabel.setText("💬 Nouveau message de " + from);
                }
            }
            return;
        }

        if (line.startsWith("HISTORY_BEGIN|")) {
            inHistory = true;
            historyBuffer.clear();
            return;
        }

        if (line.startsWith("HISTORY_END|")) {
            inHistory = false;
            messages.clear();
            messages.addAll(historyBuffer);
            historyBuffer.clear();
            scrollToBottom();
            return;
        }

        if (line.startsWith("HISTORY|")) {
            if (inHistory) {
                String[] p = line.split("\\|", 4);
                if (p.length >= 3) {
                    String from = p[1];
                    String content = p[2];
                    String ts = p.length >= 4 ? formatTimestamp(p[3]) : "";
                    boolean mine = from.equals(AppSession.getUsername());
                    historyBuffer.add(new MessageItem(content, mine, ts));
                }
            }
            return;
        }

        if (line.startsWith("ERROR|")) {
            statusLabel.setText("⚠ " + line.substring("ERROR|".length()));
            return;
        }

        if (line.startsWith("OK|SEND")) {
            statusLabel.setText("✓ Envoyé");
        }
    }

    /** Ajoute un message à la liste et fait défiler vers le bas */
    private void addMessage(String content, boolean isMine, String ts) {
        messages.add(new MessageItem(content, isMine, ts));
        scrollToBottom();
    }

    /** Fait défiler la ListView vers le dernier message */
    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            messagesListView.scrollTo(messages.size() - 1);
        }
    }

    /** Formate un timestamp LocalDateTime.toString() en "HH:mm" */
    private String formatTimestamp(String raw) {
        try {
            if (raw != null && raw.contains("T")) {
                String time = raw.split("T")[1];
                if (time.length() >= 5) return time.substring(0, 5);
            }
        } catch (Exception ignored) {}
        return raw != null ? raw : "";
    }

    /** Retourne l'heure courante au format "HH:mm" */
    private String nowTime() {
        return java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }
}

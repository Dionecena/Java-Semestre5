package com.example.whatsapp_java.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * Cellule personnalisée pour afficher les messages en bulles style WhatsApp.
 *
 * Messages envoyés (isMine=true)  → bulle verte alignée à DROITE
 * Messages reçus  (isMine=false) → bulle grise alignée à GAUCHE
 */
public class MessageCell extends ListCell<MessageItem> {

    // Couleurs WhatsApp dark
    private static final String COLOR_SENT     = "#005C4B"; // vert foncé
    private static final String COLOR_RECEIVED = "#1F2C34"; // gris foncé
    private static final String COLOR_TEXT      = "#E9EDEF"; // blanc cassé
    private static final String COLOR_TIMESTAMP = "#8696A0"; // gris clair

    @Override
    protected void updateItem(MessageItem item, boolean empty) {
        super.updateItem(item, empty);

        // Fond de la cellule toujours transparent
        setStyle("-fx-background-color: transparent; -fx-padding: 2 12;");

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        // ── Texte du message ──
        Label msgLabel = new Label(item.getContent());
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(420);
        msgLabel.setStyle(
                "-fx-text-fill: " + COLOR_TEXT + ";" +
                "-fx-font-size: 13px;"
        );
        msgLabel.setTextAlignment(TextAlignment.LEFT);

        // ── Timestamp ──
        Label tsLabel = new Label(item.getTimestamp());
        tsLabel.setStyle(
                "-fx-text-fill: " + COLOR_TIMESTAMP + ";" +
                "-fx-font-size: 10px;"
        );

        // ── Bulle (VBox contenant message + timestamp) ──
        VBox bubble = new VBox(2, msgLabel, tsLabel);
        bubble.setPadding(new Insets(7, 12, 7, 12));

        if (item.isMine()) {
            // Bulle envoyée : verte, arrondie en haut-gauche + bas
            tsLabel.setAlignment(Pos.CENTER_RIGHT);
            bubble.setAlignment(Pos.CENTER_RIGHT);
            bubble.setStyle(
                    "-fx-background-color: " + COLOR_SENT + ";" +
                    "-fx-background-radius: 12 0 12 12;"
            );
        } else {
            // Bulle reçue : grise, arrondie en haut-droit + bas
            tsLabel.setAlignment(Pos.CENTER_LEFT);
            bubble.setAlignment(Pos.CENTER_LEFT);
            bubble.setStyle(
                    "-fx-background-color: " + COLOR_RECEIVED + ";" +
                    "-fx-background-radius: 0 12 12 12;"
            );
        }

        // ── Conteneur ligne (HBox) pour aligner la bulle ──
        HBox row = new HBox(bubble);
        row.setPadding(new Insets(2, 0, 2, 0));

        if (item.isMine()) {
            row.setAlignment(Pos.CENTER_RIGHT);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
        }

        setGraphic(row);
        setText(null);
    }
}

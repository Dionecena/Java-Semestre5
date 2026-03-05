package com.example.whatsapp_java.client;

/**
 * DTO représentant un message dans la ListView de chat.
 * isMine = true  → message envoyé par moi (bulle droite, verte)
 * isMine = false → message reçu (bulle gauche, grise)
 */
public class MessageItem {

    private final String content;
    private final boolean isMine;
    private final String timestamp; // ex: "14:32" ou ""

    public MessageItem(String content, boolean isMine, String timestamp) {
        this.content = content;
        this.isMine = isMine;
        this.timestamp = timestamp == null ? "" : timestamp;
    }

    public String getContent() {
        return content;
    }

    public boolean isMine() {
        return isMine;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

package com.example.whatsapp_java.client;

public class AppSession {
    private static String username;
    private static ChatClient client;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        AppSession.username = username;
    }

    public static ChatClient getClient() {
        return client;
    }

    public static void setClient(ChatClient client) {
        AppSession.client = client;
    }
}

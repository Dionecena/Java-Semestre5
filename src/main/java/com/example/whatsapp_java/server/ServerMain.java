package com.example.whatsapp_java.server;

public class ServerMain {
    public static void main(String[] args) {
        int port = 5000;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}

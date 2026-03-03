package com.example.whatsapp_java.server;

import com.example.whatsapp_java.dao.UserDAO;
import com.example.whatsapp_java.entities.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatServer {

    private final int port;
    private final Map<String, ClientHandler> activeUsers = new ConcurrentHashMap<>();
    private final UserDAO userDAO = new UserDAO();

    public ChatServer(int port) {
        this.port = port;
    }

    public Map<String, ClientHandler> getActiveUsers() {
        return activeUsers;
    }

    public void start() {
        log("Démarrage sur le port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                log("Nouvelle connexion: " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(this, socket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserOnline(String username) {
        return activeUsers.containsKey(username);
    }

    public boolean registerActive(String username, ClientHandler handler) {
        return activeUsers.putIfAbsent(username, handler) == null;
    }

    public void unregister(String username) {
        if (username != null) {
            activeUsers.remove(username);
        }
    }

    public void sendToUser(String username, String line) {
        ClientHandler handler = activeUsers.get(username);
        if (handler != null) {
            handler.sendLine(line);
        }
    }

    /** Retourne uniquement les utilisateurs en ligne (séparés par virgule) */
    public String onlineUsersCsv() {
        return String.join(",", activeUsers.keySet());
    }

    /** Retourne TOUS les utilisateurs (online ET offline) avec leur statut */
    public String allUsersCsv() {
        try {
            List<User> all = userDAO.findAll();
            return all.stream()
                    .map(u -> u.getUsername() + ":" + (activeUsers.containsKey(u.getUsername()) ? "ONLINE" : "OFFLINE"))
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            // Fallback: retourner seulement les online si la DB est inaccessible
            return activeUsers.keySet().stream()
                    .map(u -> u + ":ONLINE")
                    .collect(Collectors.joining(","));
        }
    }

    public static void log(String message) {
        System.out.printf("[%s] [SERVER] %s%n",
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                message);
    }
}

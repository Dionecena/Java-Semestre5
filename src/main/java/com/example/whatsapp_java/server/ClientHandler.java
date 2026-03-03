package com.example.whatsapp_java.server;

import com.example.whatsapp_java.dao.MessageDAO;
import com.example.whatsapp_java.dao.UserDAO;
import com.example.whatsapp_java.entities.*;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler extends Thread {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ChatServer server;
    private final Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    private String username;

    private final UserDAO userDAO = new UserDAO();
    private final MessageDAO messageDAO = new MessageDAO();

    public ClientHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    // RG12 – journalisation structurée avec timestamps
    private void log(String message) {
        System.out.printf("[%s] [SERVER] %s%n",
                LocalDateTime.now().format(FMT), message);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String line;
            while ((line = in.readLine()) != null) {
                handleLine(line);
            }
        } catch (Exception e) {
            log("Perte connexion: " + (username != null ? username : socket.getRemoteSocketAddress())
                    + " – " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void sendLine(String line) {
        if (out != null) {
            out.println(line);
        }
    }

    private void handleLine(String line) {
        // Format: COMMAND|arg1|arg2...
        String[] parts = line.split("\\|", -1);
        String cmd = parts[0];

        switch (cmd) {
            case "REGISTER"  -> handleRegister(parts);
            case "LOGIN"     -> handleLogin(parts);
            case "LOGOUT"    -> handleLogout();
            case "USERS"     -> sendLine("USERS|" + server.onlineUsersCsv());
            case "ALL_USERS" -> sendLine("ALL_USERS|" + server.allUsersCsv());
            case "SEND"      -> handleSend(parts);
            case "HISTORY"   -> handleHistory(parts);
            default          -> sendLine("ERROR|Commande inconnue: " + cmd);
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 3) {
            sendLine("ERROR|Format REGISTER invalide");
            return;
        }
        String u = parts[1].trim();
        String passHash = parts[2].trim();

        if (u.isEmpty() || passHash.isEmpty()) {
            sendLine("ERROR|Champs vides");
            return;
        }

        if (userDAO.findByUsername(u) != null) {
            sendLine("ERROR|Username déjà utilisé");
            return;
        }

        User user = new User(null, u, passHash, UserStatus.OFFLINE, LocalDateTime.now());
        userDAO.save(user);
        log("Inscription: " + u);
        sendLine("OK|REGISTER");
    }

    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            sendLine("ERROR|Format LOGIN invalide");
            return;
        }
        String u = parts[1].trim();
        String passHash = parts[2].trim();

        User user = userDAO.findByUsername(u);
        if (user == null) {
            sendLine("ERROR|Utilisateur introuvable");
            return;
        }
        if (!user.getPassword().equals(passHash)) {
            sendLine("ERROR|Mot de passe incorrect");
            return;
        }

        // RG3: un seul login à la fois
        if (server.isUserOnline(u)) {
            sendLine("ERROR|Utilisateur déjà connecté");
            return;
        }

        boolean ok = server.registerActive(u, this);
        if (!ok) {
            sendLine("ERROR|Utilisateur déjà connecté");
            return;
        }

        this.username = u;
        userDAO.updateStatus(u, UserStatus.ONLINE);
        log("Connexion: " + u);

        sendLine("OK|LOGIN");

        // Envoyer la liste complète (online + offline) pour permettre d'écrire à quelqu'un d'offline (RG6)
        sendLine("ALL_USERS|" + server.allUsersCsv());

        // RG6: livrer messages en attente
        List<Message> pending = messageDAO.getNonLus(user);
        for (Message m : pending) {
            sendLine("MSG|" + m.getSender().getUsername() + "|" + m.getContenu() + "|" + m.getDateEnvoi());
            messageDAO.updateStatus(m.getId(), MessageStatus.LU);
        }
        if (!pending.isEmpty()) {
            log("Messages en attente livrés à " + u + ": " + pending.size());
        }

        // Notifier les autres utilisateurs connectés
        broadcastAllUsers();
    }

    private void handleLogout() {
        log("Déconnexion volontaire: " + (username != null ? username : "inconnu"));
        sendLine("OK|LOGOUT");
        cleanup();
    }

    private void handleSend(String[] parts) {
        if (username == null) {
            sendLine("ERROR|Non authentifié");
            return;
        }
        if (parts.length < 3) {
            sendLine("ERROR|Format SEND invalide");
            return;
        }
        String to = parts[1].trim();
        String content = parts[2];

        if (content == null || content.trim().isEmpty()) {
            sendLine("ERROR|Message vide");
            return;
        }
        if (content.length() > 1000) {
            sendLine("ERROR|Message trop long (max 1000 caractères)");
            return;
        }

        User sender = userDAO.findByUsername(username);
        User receiver = userDAO.findByUsername(to);
        if (receiver == null) {
            sendLine("ERROR|Destinataire introuvable");
            return;
        }

        Message message = new Message(null, sender, receiver, content, LocalDateTime.now(), MessageStatus.ENVOYE);
        messageDAO.save(message);
        log("Message: " + username + " -> " + to + " (" + content.length() + " chars)");

        // Livraison temps réel si le destinataire est en ligne
        if (server.isUserOnline(to)) {
            server.sendToUser(to, "MSG|" + username + "|" + content + "|" + message.getDateEnvoi());
            messageDAO.updateStatus(message.getId(), MessageStatus.RECU);
        }

        sendLine("OK|SEND");
    }

    private void handleHistory(String[] parts) {
        if (username == null) {
            sendLine("ERROR|Non authentifié");
            return;
        }
        if (parts.length < 2) {
            sendLine("ERROR|Format HISTORY invalide");
            return;
        }
        String other = parts[1].trim();
        User u1 = userDAO.findByUsername(username);
        User u2 = userDAO.findByUsername(other);
        if (u1 == null || u2 == null) {
            sendLine("ERROR|Utilisateur introuvable");
            return;
        }

        List<Message> hist = messageDAO.getHistorique(u1, u2);
        sendLine("HISTORY_BEGIN|");
        for (Message m : hist) {
            sendLine("HISTORY|" + m.getSender().getUsername() + "|" + m.getContenu() + "|" + m.getDateEnvoi());
        }
        sendLine("HISTORY_END|");
        log("Historique envoyé à " + username + " pour conversation avec " + other + " (" + hist.size() + " messages)");
    }

    /** Diffuse la liste complète (online+offline) à tous les clients connectés */
    private void broadcastAllUsers() {
        String allUsersLine = "ALL_USERS|" + server.allUsersCsv();
        for (ClientHandler h : server.getActiveUsers().values()) {
            h.sendLine(allUsersLine);
        }
    }

    private void cleanup() {
        if (username != null) {
            log("Déconnexion: " + username);
            userDAO.updateStatus(username, UserStatus.OFFLINE);
            server.unregister(username);
            broadcastAllUsers();
            username = null;
        }
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {
        }
    }
}

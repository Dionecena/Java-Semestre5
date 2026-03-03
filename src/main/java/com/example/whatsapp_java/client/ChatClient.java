package com.example.whatsapp_java.client;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    public interface Listener {
        void onLineReceived(String line);
        void onDisconnected(Exception e);
    }

    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread readerThread;

    private volatile Listener listener;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void connect(Listener listener) throws IOException {
        this.listener = listener;
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    Listener l = this.listener;
                    if (l != null) {
                        l.onLineReceived(line);
                    }
                }
                Listener l = this.listener;
                if (l != null) {
                    l.onDisconnected(null);
                }
            } catch (Exception e) {
                Listener l = this.listener;
                if (l != null) {
                    l.onDisconnected(e);
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void sendLine(String line) {
        if (out != null) {
            out.println(line);
        }
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {
        }
    }
}

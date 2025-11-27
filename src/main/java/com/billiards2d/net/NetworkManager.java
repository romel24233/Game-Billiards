package com.billiards2d.net;

import com.billiards2d.Vector2D;
import com.billiards2d.core.GameBus;
import javafx.application.Platform;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkManager {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean running = true;

    public void startServer(int port) {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                System.out.println("Server Waiting...");
                socket = ss.accept();
                setupStreams();
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    public void connectClient(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                setupStreams();
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    private void setupStreams() throws IOException {
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        new Thread(this::listen).start();

        GameBus.subscribe(GameBus.EventType.SHOT_TAKEN, payload -> {
            try {
                Vector2D f = (Vector2D) payload;
                out.writeByte(1);
                out.writeDouble(f.getX());
                out.writeDouble(f.getY());
                out.flush();
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    private void listen() {
        while (running && socket != null && !socket.isClosed()) {
            try {
                if (in.readByte() == 1) {
                    double x = in.readDouble();
                    double y = in.readDouble();
                    Platform.runLater(() -> GameBus.publish(GameBus.EventType.REMOTE_SHOT, new Vector2D(x, y)));
                }
            } catch (IOException e) { running = false; }
        }
    }
}
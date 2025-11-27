// src/main/java/com/billiards2d/net/NetworkClient.java
package com.billiards2d.net;

import com.billiards2d.CueBall;
import com.billiards2d.Vector2D;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;

public class NetworkClient {
    private PrintWriter out;
    private CueBall cueBall;

    public NetworkClient(String host, CueBall cueBall) {
        this.cueBall = cueBall;
        try {
            Socket socket = new Socket(host, 5000);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Thread untuk mendengarkan pesan masuk
            new Thread(() -> listen(socket)).start();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void sendShot(Vector2D force) {
        if (out != null) {
            out.println("SHOT:" + force.getX() + "," + force.getY());
        }
    }

    private void listen(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while ((msg = in.readLine()) != null) {
                if (msg.startsWith("SHOT:")) {
                    String[] parts = msg.split(":")[1].split(",");
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);

                    // Eksekusi di JavaFX Thread
                    Platform.runLater(() -> {
                        cueBall.hit(new Vector2D(x, y));
                    });
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
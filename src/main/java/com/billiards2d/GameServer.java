// src/main/java/com/billiards2d/net/GameServer.java
package com.billiards2d.net;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static List<PrintWriter> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started on port 5000...");

        while (true) {
            Socket socket = serverSocket.accept();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            clients.add(out);
            new Thread(new ClientHandler(socket)).start();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        public ClientHandler(Socket socket) { this.socket = socket; }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received: " + inputLine);
                    // Broadcast ke semua client lain
                    for (PrintWriter writer : clients) {
                        writer.println(inputLine);
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
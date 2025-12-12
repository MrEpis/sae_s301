package app.service;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class NetworkService {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    private Consumer<String> notificationListener;

    private static final String SERVER_HOST = "192.168.22.7";
    private static final int SERVER_PORT = 8080;
    private boolean isRunning = false;

    public NetworkService() {
        try {
            this.socket = new Socket(SERVER_HOST, SERVER_PORT);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connexion établie.");

            startListening();

        } catch (IOException e) {
            System.err.println("Erreur connexion: " + e.getMessage());
        }
    }

    public void setNotificationListener(Consumer<String> listener) {
        this.notificationListener = listener;
    }

    private void startListening() {
        isRunning = true;
        Thread listeningThread = new Thread(() -> {
            try {
                String message;
                while (isRunning && (message = in.readLine()) != null) {
                    System.out.println("[RECU BRUT] : " + message);

                    if (message.contains("\"type\": \"response\"") || message.contains("\"type\":\"response\"")) {
                        responseQueue.put(message);
                    }
                    else {
                        String finalMessage = message;
                        if (notificationListener != null) {
                            Platform.runLater(() -> notificationListener.accept(finalMessage));
                        }
                    }
                }
            } catch (Exception e) {
                if (isRunning) System.out.println("Connexion interrompue : " + e.getMessage());
            }
        });
        listeningThread.setDaemon(true);
        listeningThread.start();
    }

    public String sendRequest(String jsonRequest) {
        if (socket == null || socket.isClosed()) return null;

        try {
            System.out.println("[ENVOI] : " + jsonRequest);
            out.println(jsonRequest);

            return responseQueue.take();

        } catch (InterruptedException e) {
            System.err.println("Erreur d'attente de réponse.");
            return null;
        }
    }

    public void closeConnection() {
        isRunning = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
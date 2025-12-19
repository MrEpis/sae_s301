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

// Handles TCP socket communication and background message listening
public class NetworkService {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private Consumer<String> notificationListener;
    private boolean isRunning = false;

    private static final String SERVER_HOST = System.getProperty("server.addr", "134.59.27.129");
    private static final int SERVER_PORT = 8080;

    // Initializes connection to the server host and port
    public NetworkService() {
        try {
            this.socket = new Socket(SERVER_HOST, SERVER_PORT);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            startListening();
        } catch (IOException e) {
            System.err.println("Erreur connexion sur " + SERVER_HOST + " : " + e.getMessage());
        }
    }

    // Assigns a callback for handling incoming asynchronous notifications
    public void setNotificationListener(Consumer<String> listener) {
        this.notificationListener = listener;
    }

    // Starts a dedicated thread to continuously read server messages
    private void startListening() {
        isRunning = true;
        Thread listeningThread = new Thread(() -> {
            try {
                String message;
                while (isRunning && (message = in.readLine()) != null) {
                    System.out.println("[RECU BRUT] : " + message);

                    if (message.contains("TradeResult") || message.contains("FightResult")) {
                        notifyListener(message);
                    } else if (message.contains("\"type\": \"response\"") || message.contains("\"type\":\"response\"")) {
                        responseQueue.put(message);
                    } else {
                        notifyListener(message);
                    }
                }
            } catch (Exception e) {
                if (isRunning) System.out.println("Connexion interrompue.");
            }
        });
        listeningThread.setDaemon(true);
        listeningThread.start();
    }

    // Sends received messages back to the JavaFX UI thread
    private void notifyListener(String message) {
        if (notificationListener != null) {
            Platform.runLater(() -> notificationListener.accept(message));
        }
    }

    // Sends a request and waits synchronously for a response
    public String sendRequest(String jsonRequest) {
        if (socket == null || socket.isClosed()) return null;
        try {
            System.out.println("[ENVOI SYNC] : " + jsonRequest);
            out.println(jsonRequest);
            return responseQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    // Sends a message to the server without waiting for a reply
    public void sendMessage(String jsonMessage) {
        if (socket != null && !socket.isClosed()) {
            System.out.println("[ENVOI ASYNC] : " + jsonMessage);
            out.println(jsonMessage);
        }
    }

    // Safely terminates the network thread and closes the socket
    public void closeConnection() {
        isRunning = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
        }
    }
}
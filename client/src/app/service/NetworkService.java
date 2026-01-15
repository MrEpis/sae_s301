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

/**
 * Service class handling socket communication with the game server.
 * It manages asynchronous listening for notifications and synchronous request-response patterns
 * using thread-safe blocking queues.
 */
public class NetworkService {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private Consumer<String> notificationListener;
    private boolean isRunning = false;

    private static final String SERVER_HOST = System.getProperty("server.addr", "134.59.27.129");
    private static final int SERVER_PORT = 8080;

    /**
     * Initializes the service and attempts to establish a socket connection.
     * Starts the background listening thread if successful.
     */
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

    /**
     * Starts a background thread to continuously read incoming lines from the server.
     * Dispatches messages to the response queue or notification listener based on their type.
     */
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

    /**
     * Forwards a notification to the listener using Platform.runLater for UI thread safety.
     * @param message The message to notify.
     */
    private void notifyListener(String message) {
        if (notificationListener != null) {
            Platform.runLater(() -> notificationListener.accept(message));
        }
    }

    /**
     * Sends a request to the server and blocks until a response is received.
     * @param jsonRequest The request JSON string to send.
     * @return The server's response JSON, or null if interrupted.
     */
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

    /**
     * Sends a message asynchronously without waiting for a direct response.
     * Useful for sending reactions to server notifications.
     * @param jsonMessage The JSON message to send.
     */
    public void sendMessage(String jsonMessage) {
        if (socket != null && !socket.isClosed()) {
            System.out.println("[ENVOI ASYNC] : " + jsonMessage);
            out.println(jsonMessage);
        }
    }

    /**
     * Properly closes the socket connection and stops the listening thread.
     */
    public void closeConnection() {
        isRunning = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
        }
    }

    public void setNotificationListener(Consumer<String> listener) {
        this.notificationListener = listener;
    }
}
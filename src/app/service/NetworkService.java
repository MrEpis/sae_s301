package app.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkService {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private static final String SERVER_HOST = "134.59.27.129"; // Localhost
    private static final int SERVER_PORT = 8080; // Port d'écoute du serveur C

    public NetworkService() {
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), 2000);

            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connexion au serveur établie sur " + SERVER_HOST + ":" + SERVER_PORT);

        } catch (IOException e) {
            System.err.println("Erreur: Impossible de se connecter au serveur.");
            e.printStackTrace();
        }
    }


    public String sendRequest(String jsonRequest) {
        if (socket == null || socket.isClosed()) {
            System.err.println("Erreur: Pas de connexion au serveur.");
            return null;
        }

        try {
            System.out.println("[CLIENT -> SERVEUR] : " + jsonRequest);
            out.println(jsonRequest);

            String response = in.readLine();
            System.out.println("[SERVEUR -> CLIENT] : " + response);

            return response;

        } catch (IOException e) {
            System.err.println("Erreur lors de la communication.");
            e.printStackTrace();
            return null;
        }
    }


    public void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
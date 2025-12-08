package app.model;

import java.io.*;
import java.net.Socket;

public class NetworkService {

    private static final String SERVER_IP = "127.0.0.1"; // L'adresse du serveur C
    private static final int SERVER_PORT = 12345;       // Le port d'écoute du serveur C

    public String sendRequest(String request) {
        try (
                // Établir la connexion
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);

                // Préparer les flux d'entrée/sortie
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Envoyer la requête au serveur C
            out.println(request);

            // Lire la réponse (lecture bloquante jusqu'à ce que le serveur réponde)
            return in.readLine();

        } catch (IOException e) {
            System.err.println("Erreur de communication avec le serveur C : " + e.getMessage());
            return null; // En cas d'échec
        }
    }
}
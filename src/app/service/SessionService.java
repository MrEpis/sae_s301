package app.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SessionService {

    private static final String FILE_NAME = "session.dat";

    // Récupère l'ID stocké. Retourne 0 si le fichier n'existe pas.
    public static int loadClientId() {
        Path path = Paths.get(FILE_NAME);
        if (Files.exists(path)) {
            try {
                String content = Files.readString(path).trim();
                return Integer.parseInt(content);
            } catch (Exception e) {
                System.err.println("Erreur lecture session : " + e.getMessage());
            }
        }
        return 0; // 0 = Pas d'ID, donc nouvel utilisateur
    }

    // Sauvegarde l'ID reçu du serveur
    public static void saveClientId(int id) {
        try {
            Files.writeString(Paths.get(FILE_NAME), String.valueOf(id));
            System.out.println("ID Client " + id + " sauvegardé.");
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde session : " + e.getMessage());
        }
    }
}
package app.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SessionService {

    private static final String FILE_NAME = "session.dat";

    public static int loadClientId() {
        if ("true".equals(System.getProperty("nosession"))) {
            return 0;
        }
        Path path = Paths.get(FILE_NAME);
        if (Files.exists(path)) {
            try {
                String content = Files.readString(path).trim();
                return Integer.parseInt(content);
            } catch (Exception e) {
                System.err.println("Erreur lecture session : " + e.getMessage());
            }
        }
        return 0;
    }

    public static void saveClientId(int id) {
        if ("true".equals(System.getProperty("nosession"))) {
            return;
        }
        try {
            Files.writeString(Paths.get(FILE_NAME), String.valueOf(id));
            System.out.println("ID Client " + id + " sauvegardé.");
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde session : " + e.getMessage());
        }
    }

    public static void clearSession() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }
}
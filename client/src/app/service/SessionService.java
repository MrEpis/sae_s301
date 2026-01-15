package app.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service responsible for persisting user session data across application restarts.
 * It stores the unique client ID in a local file ("session.dat").
 */
public class SessionService {

    private static final String FILE_NAME = "session.dat";

    /**
     * Attempts to load a previously saved client ID from the storage file.
     * Respects the "nosession" system property for testing purposes.
     * @return The stored client ID, or 0 if no session is found.
     */
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

    /**
     * Persists the given client ID into the local session file.
     * @param id The ID to save for future reconnects.
     */
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

    /**
     * Deletes the local session file, effectively logging the user out of the current device.
     */
    public static void clearSession() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }
}
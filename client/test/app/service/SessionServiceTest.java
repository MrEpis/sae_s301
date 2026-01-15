package app.service;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le service SessionService.
 * Vérifie que l'ID client est correctement persisté sur le disque.
 */
class SessionServiceTest {

    @Test
    void testSaveAndLoadSession() {
        int testId = 999;

        SessionService.saveClientId(testId);

        // Vérification de l'existence physique du fichier
        File sessionFile = new File("session.dat");
        assertTrue(sessionFile.exists(), "Le fichier session.dat devrait avoir été créé.");

        int loadedId = SessionService.loadClientId();
        assertEquals(testId, loadedId, "L'ID chargé ne correspond pas à l'ID sauvegardé.");
    }

    @Test
    void testLoadEmptySession() {
        // On nettoie la session pour tester le cas "vide"
        SessionService.clearSession();

        int loadedId = SessionService.loadClientId();
        assertEquals(0, loadedId, "Une session inexistante devrait retourner 0.");
    }

    @Test
    void testClearSession() {
        SessionService.saveClientId(555);
        SessionService.clearSession();

        File sessionFile = new File("session.dat");
        assertFalse(sessionFile.exists(), "Le fichier ne devrait plus exister après clearSession.");
    }
}
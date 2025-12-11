package app.service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class SessionServiceTest {
    private final String FILE_NAME = "session.dat";

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of(FILE_NAME));
    }

    @Test
    void testSaveAndLoadClientId(){
        int idToSave = 999;
        SessionService.saveClientId(idToSave);

        File file = new File(FILE_NAME);
        assertTrue(file.exists(), "Le fichier dession.dat aurait dû être créé");

        int loadedId = SessionService.loadClientId();

        assertEquals(idToSave, loadedId, "L'ID chargé ne correspond pas à l'ID sauvegardé");
    }

    @Test
    void testLoadClientId_WhenFileDoesNotExist(){
        try { Files.deleteIfExists(Path.of(FILE_NAME)); } catch (IOException e) {}
        int id = SessionService.loadClientId();
        assertEquals(0, id, "Si le fichier n'existe pas, l'ID doit être 0");
    }
}

package app.service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class SessionServiceTest {
    private final String FILE_NAME = "session.dat";

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(Path.of(FILE_NAME));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of(FILE_NAME));
    }

    @Test
    void testSaveAndLoadClientId(){
        int idToSave = 12345;
        SessionService.saveClientId(idToSave);

        assertTrue(new File(FILE_NAME).exists());
        assertEquals(idToSave, SessionService.loadClientId());
    }

}

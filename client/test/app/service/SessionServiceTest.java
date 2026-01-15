package app.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {
    // The filename must match the one defined in SessionService
    private final String FILE_NAME = "session.dat";

    /**
     * Setup method executed before each test.
     * Ensures a clean state by deleting the session file if it exists.
     */
    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(Path.of(FILE_NAME));
        System.clearProperty("nosession"); // Ensure the system property is cleared
    }

    /**
     * Teardown method executed after each test.
     * Cleans up the environment by deleting the session file.
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of(FILE_NAME));
        System.clearProperty("nosession");
    }

    /**
     * Tests the saveClientId and loadClientId methods.
     * Verifies that a client ID can be persisted to the file and retrieved correctly.
     */
    @Test
    void testSaveAndLoadClientId() {
        int idToSave = 12345;

        // Test Saving
        SessionService.saveClientId(idToSave);
        assertTrue(new File(FILE_NAME).exists(), "The session.dat file should exist after saving the ID");

        // Test Loading
        int loadedId = SessionService.loadClientId();
        assertEquals(idToSave, loadedId, "The loaded ID should match the ID that was saved");
    }

    /**
     * Tests loadClientId behavior when the session file does not exist.
     * Verifies that the method returns 0 (indicating no active session) instead of throwing an error.
     */
    @Test
    void testLoadWhenFileMissing() {
        // The file is ensured to be missing by setUp()
        int id = SessionService.loadClientId();
        assertEquals(0, id, "Should return 0 if the session file does not exist");
    }

    /**
     * Tests the clearSession method.
     * Verifies that the session file is strictly deleted from the disk.
     */
    @Test
    void testClearSession() {
        // 1. Create a dummy session file
        SessionService.saveClientId(55);
        assertTrue(new File(FILE_NAME).exists(), "Pre-condition: File should exist before clearing");

        // 2. Clear the session
        SessionService.clearSession();

        // 3. Verify deletion
        assertFalse(new File(FILE_NAME).exists(), "The session file should be deleted after clearSession() is called");
    }

    /**
     * Tests the behavior when the "nosession" system property is set to "true".
     * Verifies that file operations are bypassed (no save, load returns 0).
     */
    @Test
    void testNoSessionMode() {
        // Enable "nosession" mode
        System.setProperty("nosession", "true");

        // Attempt to save
        SessionService.saveClientId(999);
        assertFalse(new File(FILE_NAME).exists(), "File should NOT be created when 'nosession' property is true");

        // Attempt to load
        int id = SessionService.loadClientId();
        assertEquals(0, id, "Should return 0 regardless of file state when 'nosession' property is true");
    }
}
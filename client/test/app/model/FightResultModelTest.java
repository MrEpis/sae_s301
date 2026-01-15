package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FightResultModelTest {

    /**
     * Tests the initialization of the FightResultModel.
     * Verifies that the log message and the opponent's card are correctly stored via the constructor,
     * and that the user's card (myCard) is initialized to null by default.
     */
    @Test
    void testInitialization() {
        // Create a dummy card for the opponent (ID: 2, Name: Enemy)
        // Constructor signature based on your Card class: id, name, hp, def, atk, imagePath
        Card opponentCard = new Card(2, "Enemy", 100, 10, 10, "src/ressources/img/demon.png");

        // Initialize the model with a log message and the opponent card
        String logMessage = "Battle started!";
        FightResultModel result = new FightResultModel(logMessage, opponentCard);

        // Verify that the getters return the expected values
        assertEquals(logMessage, result.getLogMessage(), "Log message should match the constructor argument");
        assertEquals(opponentCard, result.getOpponentCard(), "Opponent card should match the constructor argument");

        // Verify that myCard is null initially (as it is not passed in the constructor)
        assertNull(result.getMyCard(), "MyCard field should be null until explicitly set");
    }

    /**
     * Tests the setMyCard method.
     * Verifies that the user's card can be correctly assigned to the model after initialization.
     */
    @Test
    void testSetMyCard() {
        // Initialize the model with dummy values
        FightResultModel result = new FightResultModel("Log", null);

        // Create a dummy card for the player (ID: 1, Name: Hero)
        Card myCard = new Card(1, "Hero", 100, 20, 20, "src/ressources/img/sword.png");

        // Set the player's card
        result.setMyCard(myCard);

        // Verify that the card was correctly stored
        assertEquals(myCard, result.getMyCard(), "The stored card should match the one passed to setMyCard");
    }
}
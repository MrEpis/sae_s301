package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TradeRequestModelTest {

    /**
     * Tests the initialization of the TradeRequestModel.
     * Verifies that IDs are correctly set via the constructor,
     * the default username format ("Joueur " + ID) is applied,
     * and the initial mode is NOT a fight.
     */
    @Test
    void testInitialization() {
        // Constructor args: initiatorId=5, initiatorCardId=100, receiverCardId=200
        TradeRequestModel req = new TradeRequestModel(5, 100, 200);

        assertEquals(5, req.getInitiatorId());
        assertEquals(100, req.getInitiatorCardId());
        assertEquals(200, req.getReceiverCardId());

        // Verify default username logic: "Joueur " + initiatorId
        assertEquals("Joueur 5", req.getInitiatorUsername(), "Default username should be 'Joueur ' + ID");

        // Verify default boolean states
        assertFalse(req.isFight(), "isFight should be false by default");
        assertFalse(req.isFightResult(), "isFightResult should be false by default (fightResult is null)");
        assertNull(req.getFightResult(), "fightResult object should be null initially");
    }

    /**
     * Tests the toString method for a standard trade proposal.
     * Verifies that the output string corresponds to a trade ("Echange proposé")
     * when isFight is false.
     */
    @Test
    void testToStringTrade() {
        TradeRequestModel req = new TradeRequestModel(1, 10, 20);
        req.setInitiatorUsername("Alice");

        // Ensure it is NOT a fight
        req.setFight(false);

        // Expected output format: "Echange proposé par Alice"
        String result = req.toString();

        assertTrue(result.contains("Echange proposé"), "toString should indicate a trade proposal");
        assertTrue(result.contains("Alice"), "toString should contain the initiator's username");
    }

    /**
     * Tests the toString method for a fight challenge.
     * Verifies that setting isFight to true changes the output string
     * to a combat challenge ("Défi de combat").
     */
    @Test
    void testToStringFight() {
        TradeRequestModel req = new TradeRequestModel(1, 10, 20);
        req.setInitiatorUsername("Bob");

        // Switch to Fight mode
        req.setFight(true);

        // Expected output format: "Défi de combat par Bob"
        String result = req.toString();

        assertTrue(result.contains("Défi de combat"), "toString should indicate a fight challenge");
        assertTrue(result.contains("Bob"), "toString should contain the initiator's username");
    }

    /**
     * Tests the storage and retrieval of a FightResultModel.
     * Verifies that:
     * 1. The result object can be stored.
     * 2. The helper method isFightResult() returns true when a result is present.
     * 3. The toString() method changes to "Résultat du combat" regardless of other flags.
     */
    @Test
    void testFightResultStorage() {
        TradeRequestModel req = new TradeRequestModel(1, 10, 20);

        // Create a dummy result (Mocking the FightResultModel behavior or using null/basic values)
        FightResultModel result = new FightResultModel("Log info", null);

        req.setFightResult(result);

        // Check state after setting result
        assertTrue(req.isFightResult(), "Should return true when a FightResultModel is not null");
        assertEquals(result, req.getFightResult(), "Should retrieve the exact FightResultModel object that was set");

        // Check priority in toString()
        assertEquals("Résultat du combat", req.toString(), "toString should display 'Résultat du combat' when a result is present");
    }

    /**
     * Tests the setters for username and fight flag.
     * Verifies that values are updated correctly.
     */
    @Test
    void testSetters() {
        TradeRequestModel req = new TradeRequestModel(1, 0, 0);

        // Test Username Setter
        req.setInitiatorUsername("NewName");
        assertEquals("NewName", req.getInitiatorUsername());

        // Test Fight Setter
        req.setFight(true);
        assertTrue(req.isFight());

        req.setFight(false);
        assertFalse(req.isFight());
    }
}
package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le modèle TradeRequestModel.
 * Vérifie la gestion des échanges, des défis de combat et des résultats.
 */
class TradeRequestModelTest {

    @Test
    void testTradeRequestCreation() {
        TradeRequestModel trade = new TradeRequestModel(1, 10, 20);

        assertFalse(trade.isFight(), "Par défaut, une requête devrait être un échange.");
        assertEquals(1, trade.getInitiatorId());
        assertEquals(10, trade.getInitiatorCardId());
        assertEquals(20, trade.getReceiverCardId());

        // Vérification du nom d'utilisateur par défaut ("Joueur " + id)
        assertEquals("Joueur 1", trade.getInitiatorUsername());
    }

    @Test
    void testSetters() {
        TradeRequestModel request = new TradeRequestModel(5, 100, 200);

        request.setInitiatorUsername("Alice");
        assertEquals("Alice", request.getInitiatorUsername());

        // Test du passage en mode combat
        request.setFight(true);
        assertTrue(request.isFight());
    }

    @Test
    void testFightResultIntegration() {
        TradeRequestModel request = new TradeRequestModel(2, 5, 10);

        // On vérifie qu'il n'y a pas de résultat au début
        assertFalse(request.isFightResult());
        assertNull(request.getFightResult());

        // On simule l'ajout d'un résultat de combat
        FightResultModel result = new FightResultModel("Victoire éclatante !", null);
        request.setFightResult(result);

        // Vérifications
        assertTrue(request.isFightResult());
        assertNotNull(request.getFightResult());
        assertEquals("Victoire éclatante !", request.getFightResult().getLogMessage());
    }

    @Test
    void testToString() {
        TradeRequestModel request = new TradeRequestModel(1, 10, 20);
        request.setInitiatorUsername("Bob");

        // Test format échange
        assertEquals("Echange proposé par Bob", request.toString());

        // Test format combat
        request.setFight(true);
        assertEquals("Défi de combat par Bob", request.toString());

        // Test format résultat
        request.setFightResult(new FightResultModel("Log", null));
        assertEquals("Résultat du combat", request.toString());
    }
}
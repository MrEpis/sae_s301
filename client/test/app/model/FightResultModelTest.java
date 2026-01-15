package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le modèle FightResultModel.
 * Vérifie que les logs de combat et les cartes impliquées sont correctement stockés.
 */
class FightResultModelTest {

    @Test
    void testFightResultData() {
        Card deadCard = new Card(5, "Golem", 0, 10, 80, "");
        String log = "Le joueur a écrasé le Golem !";

        FightResultModel result = new FightResultModel(log, deadCard);

        assertEquals(log, result.getLogMessage());
        assertNotNull(result.getOpponentCard());
        assertEquals(0, result.getOpponentCard().getHp());
        assertEquals("Golem", result.getOpponentCard().getNom());
    }

    @Test
    void testMyCardIntegration() {
        Card myCard = new Card(1, "Dragon", 20, 30, 50, "");
        FightResultModel result = new FightResultModel("Début du combat", null);

        result.setMyCard(myCard);

        assertNotNull(result.getMyCard());
        assertEquals("Dragon", result.getMyCard().getNom());
    }
}
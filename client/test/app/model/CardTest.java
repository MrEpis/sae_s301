package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testCardConstructorAndGetters() {
        // On crée une carte de test
        Card card = new Card(1, "Chevalier", 10, 50, 40, "src/ressources/img/knight.png");

        // On vérifie que les données initiales sont correctes
        assertEquals(1, card.getId());
        assertEquals("Chevalier", card.getNom());
        assertEquals(10, card.getHp());
        assertEquals(40, card.getAtk());
        assertEquals(50, card.getDef());
        assertEquals("src/ressources/img/knight.png", card.getImagePath());
    }

    @Test
    void testStatModifications() {
        Card card = new Card(1, "Baloon", 15, 80, 5, "src/ressources/img/baloon.png");

        // On simule une prise de dégâts
        card.setHp(10);
        assertEquals(10, card.getHp(), "Les HP devraient être mis à jour après un setHp");
    }
}
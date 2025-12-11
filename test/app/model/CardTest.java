package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

    class CardTest {

    @Test
    void testCardInitialization(){
        Card c = new Card("Guerrier", 100, 20, 50);

        assertEquals("Guerrier", c.getNom(), "Le nom doit servir d'identifiant");
        assertEquals(100, c.getHp());
        assertEquals(20, c.getDef());
        assertEquals(50, c.getAtk());
    }

    @Test
    void testSetHp(){
        Card c = new Card("Test", 50, 0, 0);
        c.setHp(20);
        assertEquals(20, c.getHp(), "La modification des PV a échoué");
    }
}

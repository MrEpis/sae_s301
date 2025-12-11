package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

    class CardTest {

    @Test
    void testCardInitialization(){
        String realImagePath = "src/ressources/img/sword.png";

        Card c = new Card(1,"Epée Légendaire", 40,20,40, realImagePath);

        assertEquals(1,c.getId());
        assertEquals("Epée Légendaire", c.getNom());
        assertEquals(40, c.getHp());
        assertEquals(20, c.getDef());
        assertEquals(40, c.getAtk());
        assertEquals(realImagePath, c.getImagePath());
    }

    @Test
    void testSetHp(){
        Card c = new Card(2, "Test", 50, 0, 0, null);
        c.setHp(20);
        assertEquals(20, c.getHp(), "La modification des PV a échoué");
    }
}

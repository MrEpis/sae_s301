package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    @Test
    void testPlayerInitialization(){
        Player p = new Player(0, "Guest");

        assertEquals(0, p.getId_Client());
        assertEquals("Guest", p.getName());
        assertNotNull(p.getInventory(), "Inventory should be initialized empty");
        assertTrue(p.getInventory().isEmpty());
    }

    @Test
    void testSetters(){
        Player p = new Player(0,"Guest");
        p.setId(101);
        p.setName("Robs");

        assertEquals(101, p.getId_Client());
        assertEquals("Robs", p.getName());
    }
    @Test
    void testAddCard(){
        Player p = new Player(1, "Test");

        Card c = new Card(10, "Shield", 50, 50, 0, "src/ressources/img/shield.png");
        p.addCard(c);

        assertEquals(1, p.getInventory().size());
        assertEquals("Shield", p.getInventory().get(0).getNom());
    }
}

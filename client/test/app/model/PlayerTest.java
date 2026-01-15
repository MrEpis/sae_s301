package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    /**
     * Tests the initialization of a Player object.
     * Checks that the ID and name are set, and that the inventory is initialized as empty (not null).
     */
    @Test
    void testPlayerInitialization(){
        Player p = new Player(0, "Guest");

        assertEquals(0, p.getId_Client());
        assertEquals("Guest", p.getName());
        assertNotNull(p.getInventory(), "Inventory should be initialized empty");
        assertTrue(p.getInventory().isEmpty());
    }

    /**
     * Tests the setter methods for Player properties.
     * Verifies that ID and Name can be modified after instantiation.
     */
    @Test
    void testSetters(){
        Player p = new Player(0,"Guest");
        p.setId(101);
        p.setName("Robs");

        assertEquals(101, p.getId_Client());
        assertEquals("Robs", p.getName());
    }

    /**
     * Tests adding a card to the player's inventory.
     * Verifies that the inventory size increases and contains the correct card object.
     */
    @Test
    void testAddCard(){
        Player p = new Player(1, "Test");

        Card c = new Card(10, "Shield", 50, 50, 0, "src/ressources/img/shield.png");
        p.addCard(c);

        assertEquals(1, p.getInventory().size());
        assertEquals("Shield", p.getInventory().get(0).getNom());
    }
}

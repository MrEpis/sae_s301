package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testPlayerCreation() {
        Player player = new Player(123, "Lancelot");

        assertEquals(123, player.getId_Client());
        assertEquals("Lancelot", player.getName());
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    void testAddCard() {
        Player player = new Player(1, "Test");
        Card card = new Card(10, "Dragon", 10, 40, 50, "img.png");

        player.addCard(card);

        assertEquals(1, player.getInventory().size());
        assertEquals("Dragon", player.getInventory().get(0).getNom());
    }
}
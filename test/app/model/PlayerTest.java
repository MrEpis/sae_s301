package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
    class PlayerTest {
        @Test
        void testPlayerInitialization(){
            Player p = new Player(0, "Inconnu");

            assertEquals(0, p.getId_Client());
            assertEquals("Inconnu", p.getName());
            assertNotNull(p.getInventory(), "L'inventaire ne doit jamais être null, à l'initialisation");
            assertTrue(p.getInventory().isEmpty());
        }

        @Test
        void testSetters(){
            Player p = new Player(0,"Inconnu");

            p.setId(101);
            p.setName("Robs");

            assertEquals(101, p.getId_Client());
            assertEquals("Robs", p.getName());
        }
        @Test
        void testAddCardToInventory(){
            Player p = new Player(1, "Test");

            Card c = new Card(10, "Bouclier", 50, 50, 0, "src/ressources/img/shield.png");
            p.addCard(c);

            assertEquals(1, p.getInventory().size());
            assertEquals("Bouclier", p.getInventory().get(0).getNom());
        }
}

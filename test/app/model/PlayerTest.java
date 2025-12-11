package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
    class PlayerTest {
        @Test
        void testPlaerInitialization(){
            Player p = new Player(0, "Inconnu");
            assertEquals(0, p.getId_Client());
            assertEquals("Inconnu", p.getName());
            assertNotNull(p.getInventory(), "L'inventaire ne doit jamais être null, même vide");
            assertTrue(p.getInventory().isEmpty(), "L'inventaire doit être vide au début");
        }

        @Test
        void testSettersForLogin(){
            Player p = new Player(0,"Inconnu");
            p.setId(101);
            p.setName("Robs");

            assertEquals(101, p.getId_Client());
            assertEquals("Robs", p.getName());
        }
        @Test
        void testInventoryManagement(){
            Player p = new Player(1, "Test");
            Card c = new Card("Dragon", 100, 20, 50);
            p.getInventory().add(c);

            assertEquals(1, p.getInventory().size(), "Le joueur devrait avoir 1 carte");
            assertEquals("Dragon", p.getInventory().get(0).getNom());
        }
}

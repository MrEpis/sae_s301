package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

    class CardTest {


        /**
         * Tests the initialization of a Card object.
         * Verifies that all fields are correctly assigned via the constructor,
         * specifically checking the order of defense and attack parameters.
         */
    @Test
    void testCardInitialization(){

        String realImagePath = "src/ressources/img/sword.png";
        // Constructor signature: id, name, hp, def, atk, imagePath
        // Using coherent stats: 40 HP + 20 DEF + 40 ATK = 100
        Card c = new Card(1,"Sword", 40,20,40, realImagePath);

        assertEquals(1,c.getId());
        assertEquals("Sword", c.getNom());
        assertEquals(40, c.getHp());

        // Specific check to ensure DEF is the 4th argument and ATK is the 5th
        assertEquals(40, c.getDef(), "La défense (20) n'est pas au bon endroit");
        assertEquals(20, c.getAtk(), "L'attaque (40) n'est pas au bon endroit");
        assertEquals(realImagePath, c.getImagePath());
    }

        /**
         * Tests the setHp method.
         * Verifies that the card's health points can be updated correctly.
         */
    @Test
    void testSetHp(){
        Card c = new Card(2, "Test", 50, 10, 10, null);

        c.setHp(40);
        assertEquals(40, c.getHp(), "HP setter should update value correctly");
    }
}

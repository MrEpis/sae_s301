package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    @Test
    void testGameInitialization(){
        Player p1 = new Player(1, "Héros");
        Player p2 = new Player(2, "Monstre");

        Card c1 = new Card(1, "Héros", 50, 25, 25, "src/ressources/img/sword.png");
        Card c2 = new Card(2, "Monstre", 60, 20, 20, "src/ressources/img/demon.png");

        Game game = new Game(p2, p1, c1, c2);

        assertEquals(p1, game.getPlayer1(), "Erreur : Le joueur 1 n'est pas correctement assigné");
        assertEquals(p2, game.getPlayer2(), "Erreur : Le joueur 2 n'est pas correctement assigné");

        assertEquals("Héros", game.getCardPlayer1().getNom());
        assertEquals("Monstre", game.getCardPlayer2().getNom());

        assertNull(game.getWinner());
    }

    @Test
    void testSetWinner(){
        Player p1 = new Player(1, "A");
        Player p2 = new Player(2, "B");
        Card c1 = new Card(1, "A", 10, 10, 10, null);
        Card c2 = new Card(2, "B", 10, 10, 10, null);

        Game game = new Game(p2, p1, c1, c2);
        game.setWinner(p1);

        assertEquals(p1, game.getWinner());
    }
}

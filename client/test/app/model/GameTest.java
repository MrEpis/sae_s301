package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    @Test
    void testGameInitialization(){
        Player p1 = new Player(1, "Hero");
        Player villain = new Player(2, "Villain");

        Card c1 = new Card(1, "HeroCard", 50, 20, 30, "src/ressources/img/sword.png");
        Card c2 = new Card(2, "VillainCard", 60, 10, 30, "src/ressources/img/demon.png");

        // Constructor signature: Game(Player player2, Player player1, Card cardPlayer1, Card cardPlayer2)
        Game game = new Game(p2, p1, p1, c2);

        assertEquals(p1, game.getPlayer1());
        assertEquals(p2, game.getPlayer2());
        assertEquals(c1, game.getCardPlayer1().getNom());
        assertEquals(c2, game.getCardPlayer2().getNom());
        assertNull(game.getWinner(), "Winner should be null at start");
    }

    /**
     * Tests the setWinner method.
     * Verifies that the winner field is correctly updated.
     */
    @Test
    void testSetWinner(){
        Player p1 = new Player(1, "A");
        Player p2 = new Player(2, "B");
        Game game = new Game(p2, p1, null, null);

        game.setWinner(p1);
        assertEquals(p1, game.getWinner());
    }
}

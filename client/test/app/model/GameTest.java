package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    @Test
    void testGameInitialization(){
        Player hero = new Player(1, "Hero");
        Player villain = new Player(2, "Villain");

        Card heroCard = new Card(1, "HeroCard", 50, 20, 30, "src/ressources/img/sword.png");
        Card villainCard = new Card(2, "VillainCard", 60, 10, 30, "src/ressources/img/demon.png");

        Game game = new Game(villain, hero, heroCard, villainCard);

        assertEquals(hero, game.getPlayer1());
        assertEquals(villain, game.getPlayer2());
        assertEquals(heroCard, game.getCardPlayer1().getNom());
        assertEquals(villainCard, game.getCardPlayer2().getNom());
        assertNull(game.getWinner(), "Winner should be null at start");
    }

    @Test
    void testSetWinner(){
        Player p1 = new Player(1, "A");
        Player p2 = new Player(2, "B");
        Game game = new Game(p2, p1, null, null);

        game.setWinner(p1);
        assertEquals(p1, game.getWinner());
    }
}

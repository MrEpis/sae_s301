package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FightResultModelTest {

    @Test
    void testSimultaneousDamageResult(){
        Card myCard = new Card(1,"MyHero",50, 20, 30, "hero.png");

        Card opponentCard = new Card(2, "Enemy", 40, 10, 50, "enemy.png");

        myCard.setHp(20);
        opponentCard.setHp(20);
        String log = "Both cards attacked! HP updated.";
        FightResultModel result = new FightResultModel(log, opponentCard);
        result.setMyCard(myCard);

        assertEquals(log, result.getLogMessage());
        assertEquals(20, result.getMyCard().getHp());
        assertEquals("MyHero", result.getMyCard().getNom());
        assertEquals("Enemy", result.getOpponentCard().getNom());
    }
}

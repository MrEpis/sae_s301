package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TradeRequestModelTest {

    @Test
    void testInitialization(){
        TradeRequestModel req = new TradeRequestModel(5, 100, 200);

        assertEquals(5, req.getInitiatorId());
        assertEquals(100, req.getInitiatorCardId());
        assertEquals(200, req.getReceiverCardId());
        assertEquals("Player 5", req.getInitiatorUsername());

        assertFalse(req.isFight());
        assertFalse(req.isFightResult());
    }

    @Test
    void testCombatScenario(){
        TradeRequestModel req = new TradeRequestModel(1, 10 ,20);
        req.setInitiatorUsername("Challenger");

        req.setFight(true);
        assertTrue(req.isFight());
        assertTrue(req.toString().contains("Combat challenge"));
    }

    @Test
    void testFightResultTransmission(){
        TradeRequestModel req = new TradeRequestModel(1, 10, 20);
        FightResultModel result = new FightResultModel("Combat finished", null);

        req.setFightResult(result);

        assertTrue(req.isFightResult());
        assertEquals(result, req.getFightResult());
        assertEquals("Résultat du combat", req.toString());
    }
}

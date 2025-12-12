package app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TradeTest {

    @Test
    void testTradeInitialization(){
        Player requester = new Player(1, "Alice");
        Player receiver = new Player(2, "Bob");

        Card offer = new Card(1, "Potion", 10, 0, 0, "src/ressources/img/potion.png");
        Card request = new Card(2, "Coin", 0,0,0, "src/ressources/img/coin.png");

        Trade trade = new Trade(requester, receiver, offer, request);

        assertEquals(requester, trade.getRequestingPlayer());
        assertEquals(receiver, trade.getRespondingPlayer());
        assertEquals("Potion", trade.getOfferedCard().getNom());
        assertEquals("Coin", trade.getRequestedCard().getNom());

        assertFalse(trade.isAccepted(), "L'échange ne doit pas être accepté par défaut");
    }

    @Test
    void testTradeStatus(){
        Player p1 = new Player(1, "A");
        Player p2 = new Player(2, "B");

        Trade trade = new Trade(p1, p2, null, null);

        trade.setAccepted(true);
        assertTrue(trade.isAccepted());
    }
}

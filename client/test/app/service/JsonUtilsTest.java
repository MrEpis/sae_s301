package app.service;

import app.model.Card;
import app.model.FightResultModel;
import app.model.TradeRequestModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void testBuildCardCreationData() {
        String json = JsonUtils.buildCardCreationData(1, "Warrior", 50, 25, 25, "img.png");
        String expected = "{\"id_client\":\"1\", \"nomCarte\":\"Warrior\", \"pv\":50, \"attaque\":25, \"defense\":25, \"image\":\"img.png\"}";
        assertEquals(expected, json);
    }

    @Test
    void testParseFightResult() {
        String json = "{\"log\": \"Fight Over\", \"opponent_card\": {\"id\": 99, \"nom\": \"Boss\", \"pv\": 10, \"attaque\": 25, \"defense\": 25, \"image\": \"boss.png\"}}";

        FightResultModel result = JsonUtils.parseFightResult(json);

        assertEquals("Fight Over", result.getLogMessage());
        assertNotNull(result.getOpponentCard());
        assertEquals(10, result.getOpponentCard().getHp());
    }

    @Test
    void testBuildTradeResponseJson() {
        TradeRequestModel req = new TradeRequestModel(5, 10, 20);
        String json = JsonUtils.buildTradeResponseJson(true, req, 99);

        assertTrue(json.contains("\"accepted\": true"));
        assertTrue(json.contains("\"id_initiator\": 5"));
    }
}
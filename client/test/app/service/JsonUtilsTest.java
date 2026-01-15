package app.service;

import app.model.Card;
import app.model.FightResultModel;
import app.model.Player;
import app.model.TradeRequestModel;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    /**
     * Tests the buildRequest method.
     * Verifies that the JSON string is correctly formatted with the action name and data payload.
     */
    @Test
    void testBuildRequest() {
        String action = "TEST_ACTION";
        String data = "{\"key\":\"value\"}";
        String result = JsonUtils.buildRequest(action, data);

        String expected = "{\"type\":\"request\", \"nom\":\"TEST_ACTION\", \"data\":{\"key\":\"value\"}}";
        assertEquals(expected, result, "The request JSON should wrap the action and data correctly.");
    }

    /**
     * Tests the buildCardCreationData method.
     * Verifies that the card details are correctly formatted into the specific JSON structure required by the server.
     */
    @Test
    void testBuildCardCreationData() {
        // Arguments: id_client, name, hp, atk, def, imagePath
        String result = JsonUtils.buildCardCreationData(1, "Dragon", 100, 50, 40, "img.png");

        String expected = "{\"id_client\":\"1\", \"nomCarte\":\"Dragon\", \"pv\":100, \"attaque\":50, \"defense\":40, \"image\":\"img.png\"}";
        assertEquals(expected, result, "The card creation JSON should match the expected format.");
    }

    /**
     * Tests the buildLoginData method.
     * Verifies both scenarios:
     * 1. Standard login with a username.
     * 2. Reconnection where the username is null (returns ID only).
     */
    @Test
    void testBuildLoginData() {
        // Case 1: Standard Login
        String loginJson = JsonUtils.buildLoginData(1, "Alice");
        assertEquals("{\"id_client\": 1, \"username\": \"Alice\"}", loginJson, "Standard login JSON is incorrect.");

        // Case 2: Reconnection
        String reconnectJson = JsonUtils.buildLoginData(55, null);
        assertEquals("55", reconnectJson, "Reconnection string should only contain the ID.");
    }

    /**
     * Tests the buildGetOpponentInventoryRequest method.
     * Verifies the simple JSON format containing the target username.
     */
    @Test
    void testBuildGetOpponentInventoryRequest() {
        String result = JsonUtils.buildGetOpponentInventoryRequest("Bob");
        assertEquals("{\"username\": \"Bob\"}", result);
    }

    /**
     * Tests the buildTradeRequestData method.
     * Verifies that all four IDs (initiator, card, receiver, card) are correctly placed in the JSON.
     */
    @Test
    void testBuildTradeRequestData() {
        String result = JsonUtils.buildTradeRequestData(1, 10, 2, 20);
        String expected = "{\"id_initiator\": 1, \"id_card_initiator\": 10, \"id_receiver\": 2, \"id_card_receiver\": 20}";
        assertEquals(expected, result);
    }

    /**
     * Tests the parsePlayerList method.
     * Simulates a server response containing a list of players in the "data" field
     * and verifies that the list is correctly parsed into Player objects.
     */
    @Test
    void testParsePlayerList() {
        // Simulated JSON response from server
        String json = "{\"type\":\"response\", \"data\":[{\"id_client\": 10, \"username\": \"Player1\"}, {\"id_client\": 20, \"username\": \"Player2\"}]}";

        List<Player> players = JsonUtils.parsePlayerList(json);

        assertNotNull(players);
        assertEquals(2, players.size(), "Should parse 2 players.");
        assertEquals(10, players.get(0).getId_Client());
        assertEquals("Player1", players.get(0).getName());
        assertEquals(20, players.get(1).getId_Client());
    }

    /**
     * Tests the parseInventoryFromLogin method.
     * Simulates a login response with a "main" field containing cards and checks if Card objects are created.
     */
    @Test
    void testParseInventoryFromLogin() {
        // Simulated JSON with "main" array
        String json = "{\"main\":[{\"id\": 1, \"nom\": \"CardA\", \"attaque\": 10, \"defense\": 5, \"pv\": 20, \"image\": \"a.png\"}]}";

        List<Card> cards = JsonUtils.parseInventoryFromLogin(json);

        assertEquals(1, cards.size());
        assertEquals("CardA", cards.get(0).getNom());
        assertEquals(10, cards.get(0).getAtk());
    }

    /**
     * Tests the parseTradeRequestNotification method.
     * Verifies that the initiator ID and card IDs are extracted to create a TradeRequestModel.
     */
    @Test
    void testParseTradeRequestNotification() {
        String json = "{\"id_initiator\": 5, \"id_card_initiator\": 100, \"id_card_receiver\": 200}";

        TradeRequestModel model = JsonUtils.parseTradeRequestNotification(json);

        assertEquals(5, model.getInitiatorId());
        assertEquals(100, model.getInitiatorCardId());
        assertEquals(200, model.getReceiverCardId());
        assertFalse(model.isFight(), "Should be a trade request by default.");
    }

    /**
     * Tests the buildTradeResponseJson method.
     * Verifies the format of the response sent when accepting or refusing a trade.
     */
    @Test
    void testBuildTradeResponseJson() {
        TradeRequestModel req = new TradeRequestModel(1, 10, 20);
        String result = JsonUtils.buildTradeResponseJson(true, req, 2);

        // We verify the presence of key fields rather than exact string equality due to boolean formatting
        assertTrue(result.contains("\"accepted\": true"));
        assertTrue(result.contains("\"id_initiator\": 1"));
        assertTrue(result.contains("\"id_receiver\": 2"));
    }

    /**
     * Tests the parseFightRequestNotification method.
     * Verifies that it parses the data like a trade request but sets the 'fight' flag to true.
     */
    @Test
    void testParseFightRequestNotification() {
        String json = "{\"id_initiator\": 9, \"id_card_initiator\": 99, \"id_card_receiver\": 88}";

        TradeRequestModel model = JsonUtils.parseFightRequestNotification(json);

        assertEquals(9, model.getInitiatorId());
        assertTrue(model.isFight(), "The model should be marked as a fight request.");
    }

    /**
     * Tests the parseFightResult method.
     * Simulates a complex JSON response with a nested "opponent_card" object and a log message.
     * Verifies that the FightResultModel is correctly populated.
     */
    @Test
    void testParseFightResult() {
        String json = "{\"log\": \"Victory!\", \"opponent_card\": {\"id\": 7, \"nom\": \"Boss\", \"attaque\": 50, \"defense\": 50, \"pv\": 10, \"image\": \"boss.png\"}}";

        FightResultModel result = JsonUtils.parseFightResult(json);

        assertEquals("Victory!", result.getLogMessage());
        assertNotNull(result.getOpponentCard());
        assertEquals("Boss", result.getOpponentCard().getNom());
        assertEquals(10, result.getOpponentCard().getHp());
    }

    /**
     * Tests the parseUsernameFromLogin method.
     * Verifies that the username is correctly extracted from the JSON response.
     */
    @Test
    void testParseUsernameFromLogin() {
        String json = "{\"type\": \"response\", \"username\": \"MasterUser\"}";
        String username = JsonUtils.parseUsernameFromLogin(json);
        assertEquals("MasterUser", username);
    }
}
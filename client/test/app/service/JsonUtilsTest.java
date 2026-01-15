package app.service;

import app.model.Card;
import app.model.FightResultModel;
import app.model.Player;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void testBuildLoginData() {
        int id = 123;
        String username = "Titi_99";
        String result = JsonUtils.buildLoginData(id, username);

        assertTrue(result.contains("\"id_client\": 123"));
        assertTrue(result.contains("\"username\": \"Titi_99\""));
    }

    @Test
    void testParsePlayerList() {
        String json = "{\"data\": [{\"id_client\": 1, \"username\": \"Alice\"}, {\"id_client\": 2, \"username\": \"Bob\"}]}";

        List<Player> players = JsonUtils.parsePlayerList(json);

        assertNotNull(players);
        assertEquals(2, players.size());
        assertEquals("Alice", players.get(0).getName());
        assertEquals(1, players.get(0).getId_Client());
    }

    @Test
    void testParseInventoryFromLogin() {
        String json = "{\"main\": [{\"id\":10, \"nom\":\"Dracofeu\", \"pv\":100, \"attaque\":80, \"defense\":50, \"image\":\"img.png\"}]}";

        List<Card> inventory = JsonUtils.parseInventoryFromLogin(json);

        assertNotNull(inventory);
        assertEquals(1, inventory.size());
        Card card = inventory.get(0);
        assertEquals("Dracofeu", card.getNom());
        assertEquals(80, card.getAtk()); // Vérifie que "attaque" est bien mappé vers atk
    }

    @Test
    void testParseFightResult() {
        String json = "{\"log\": \"Combat gagné !\", \"opponent_card\": {\"id\":5, \"nom\":\"Golem\", \"pv\":0, \"attaque\":20, \"defense\":80, \"image\":\"g.png\"}}";

        FightResultModel result = JsonUtils.parseFightResult(json);

        assertNotNull(result);
        assertEquals("Combat gagné !", result.getLogMessage());
        assertNotNull(result.getOpponentCard());
        assertEquals("Golem", result.getOpponentCard().getNom());
    }

    @Test
    void testExtractStringInconnu() {
        String json = "{\"autre\": \"donnée\"}";
        String result = JsonUtils.parseUsernameFromLogin(json);
        assertEquals("Inconnu", result);
    }
}
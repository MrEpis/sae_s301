package app.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void testBuildLoginData() {
        String json = JsonUtils.buildLoginData(0, "Robs");
        String expected = "{\"id_client\": 0, \"username\": \"Robs\"}";
        assertEquals(expected, json, "Le JSON de login pour nouvel utilisateur est incorrect");
    }

    @Test
    void testBuildLoginDate_Reconnect() {
        String json = JsonUtils.buildLoginData(101, null);
        assertEquals("101", json, "Le format de reconnexion est incorrect");
    }


    @Test
    void testBuildCardCreationData() {
        String nomCarte = "Demon";
        int hp = 40;
        int atk = 40;
        int def = 20;
        String imagePath = "src/ressources/img/demon.png";

        String json = JsonUtils.buildCardCreationData(nomCarte, hp, atk, def, imagePath);

        String expected = "{\"nomCarte\": \"Demon\", \"hp\":40, \"attaque\":40, \"defense\":20, \"file_name\":\"src/ressources/img/demon.png\"}";
        assertEquals(expected, json, "Le JSON de création de carte ne correspond pas à la structure attendue");
    }

    @Test
    void testBuildRequest() {
        String dataPart = "{\"test\":1}";
        String fullJson = JsonUtils.buildRequest("TEST_ACTION", dataPart);

        String expected = "{\"type\":\"request\", \nom\":\"TEST_ACTION\", \"data\":{\"test\":1}}";
        assertEquals(expected, fullJson, "L'enveloppe de la requête est incorrecte");
    }
}
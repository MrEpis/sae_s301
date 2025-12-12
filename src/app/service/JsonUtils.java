package app.service;

import app.model.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {

    public static String buildRequest(String actionName, String dataJson) {
        return String.format(
                "{\"type\":\"request\", \"nom\":\"%s\", \"data\":%s}",
                actionName,
                dataJson
        );
    }

    public static String buildCardCreationData(int id_client, String nom, int pv, int atk, int def, String imagePath) {
        return String.format(
                "{\"id_client\":\"%d\", \"nomCarte\":\"%s\", \"pv\":%d, \"attaque\":%d, \"defense\":%d, \"image\":\"%s\"}",
                id_client, nom, pv, atk, def, imagePath
        );
    }

    public static String buildLoginData(int idClient, String username) {
        if (username == null) {
            return String.format("" + idClient);
        } else {
            return String.format(
                    "{\"id_client\": %d, \"username\": \"%s\"}",
                    idClient,
                    username
            );
        }
    }
    public static List<Card> parseInventoryFromLogin(String jsonResponse) {
        List<Card> cards = new ArrayList<>();

        int mainIndex = jsonResponse.indexOf("\"main\":");
        if (mainIndex == -1) return cards;

        int startIndex = jsonResponse.indexOf("[", mainIndex);
        int endIndex = jsonResponse.lastIndexOf("]");

        if (startIndex == -1 || endIndex == -1) return cards;

        String arrayContent = jsonResponse.substring(startIndex + 1, endIndex);

        String[] cardObjects = arrayContent.split("},");

        for (String cardJson : cardObjects) {
            if (cardJson.trim().isEmpty()) continue;

            int id = extractInt(cardJson, "\"id\":");
            String nom = extractString(cardJson, "\"nom\":");
            int atk = extractInt(cardJson, "\"attaque\":");
            int def = extractInt(cardJson, "\"defense\":");
            int pv = extractInt(cardJson, "\"pv\":");
            String image = extractString(cardJson, "\"image\":");

            if (image != null) image = image.replace("\"", "");

            cards.add(new Card(id, nom, pv, def, atk, image));
        }

        return cards;
    }

    private static int extractInt(String source, String key) {
        Pattern p = Pattern.compile(key + "\\s*(\\d+)");
        Matcher m = p.matcher(source);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private static String extractString(String source, String key) {
        Pattern p = Pattern.compile(key + "\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        return m.find() ? m.group(1) : "Inconnu";
    }
}
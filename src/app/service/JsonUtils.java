package app.service;

import app.model.Card;
import app.model.Player;

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

    public static String buildGetOpponentInventoryRequest(String opponentUsername) {
        return String.format(
                "{\"username\": \"%s\"}",
                opponentUsername
        );
    }

    public static String buildTradeRequestData(int initiatorId, int offeredCardId, int receiverId, int requestedCardId) {
        return String.format(
                "{\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_receiver\": %d, \"id_card_receiver\": %d}",
                initiatorId,
                offeredCardId,
                receiverId,
                requestedCardId
        );
    }

    public static List<Player> parsePlayerList(String jsonResponse) {
        List<Player> players = new ArrayList<>();

        int dataIndex = jsonResponse.indexOf("\"data\":");
        if (dataIndex == -1) return players;

        int startIndex = jsonResponse.indexOf("[", dataIndex);
        int endIndex = jsonResponse.lastIndexOf("]");

        if (startIndex == -1 || endIndex == -1) return players;

        String arrayContent = jsonResponse.substring(startIndex + 1, endIndex);
        if (arrayContent.trim().isEmpty()) return players;

        String[] playerObjects = arrayContent.split("},");

        for (String pJson : playerObjects) {
            int id = extractInt(pJson, "\"id_client\":");
            String username = extractString(pJson, "\"username\":");

            if (username != null && !username.trim().isEmpty()) {
                players.add(new Player(id, username));
            }
        }
        return players;
    }

    public static List<Card> parseOpponentInventory(String jsonResponse) {
        List<Card> cards = new ArrayList<>();
        int dataIndex = jsonResponse.indexOf("\"data\":");
        if (dataIndex == -1) return cards;

        int startIndex = jsonResponse.indexOf("[", dataIndex);
        int endIndex = jsonResponse.lastIndexOf("]");

        if (startIndex == -1 || endIndex == -1) return cards;

        String arrayContent = jsonResponse.substring(startIndex + 1, endIndex);
        if (arrayContent.trim().isEmpty()) return cards;

        String[] cardObjects = arrayContent.split("},");

        for (String cardJson : cardObjects) {
            if (cardJson.trim().isEmpty()) continue;
            cards.add(extractCardFromJson(cardJson));
        }
        return cards;
    }

    public static List<Card> parseInventoryFromLogin(String jsonResponse) {
        List<Card> cards = new ArrayList<>();
        int mainIndex = jsonResponse.indexOf("\"main\":");
        if (mainIndex == -1) return cards;

        int startIndex = jsonResponse.indexOf("[", mainIndex);
        int endIndex = jsonResponse.lastIndexOf("]");

        if (startIndex == -1 || endIndex == -1) return cards;

        String arrayContent = jsonResponse.substring(startIndex + 1, endIndex);
        if (arrayContent.trim().isEmpty()) return cards;

        String[] cardObjects = arrayContent.split("},");

        for (String cardJson : cardObjects) {
            if (cardJson.trim().isEmpty()) continue;
            cards.add(extractCardFromJson(cardJson));
        }
        return cards;
    }

    public static String parseUsernameFromLogin(String jsonResponse) {
        return extractString(jsonResponse, "\"username\":");
    }

    private static Card extractCardFromJson(String cardJson) {
        int id = extractInt(cardJson, "\"id\":");
        String nom = extractString(cardJson, "\"nom\":");
        int atk = extractInt(cardJson, "\"attaque\":");
        int def = extractInt(cardJson, "\"defense\":");
        int pv = extractInt(cardJson, "\"pv\":");
        String image = extractString(cardJson, "\"image\":");

        if (image != null) image = image.replace("\"", "");

        return new Card(id, nom, pv, def, atk, image);
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
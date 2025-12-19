package app.service;

import app.model.Card;
import app.model.FightResultModel;
import app.model.Player;
import app.model.TradeRequestModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Utility class to build and parse JSON messages for server communication
public class JsonUtils {

    // Wraps an action and its data into a standard JSON request string
    public static String buildRequest(String actionName, String dataJson) {
        return String.format(
                "{\"type\":\"request\", \"nom\":\"%s\", \"data\":%s}",
                actionName,
                dataJson
        );
    }

    // Formats card details into a JSON string for creation requests
    public static String buildCardCreationData(int id_client, String nom, int pv, int atk, int def, String imagePath) {
        return String.format(
                "{\"id_client\":\"%d\", \"nomCarte\":\"%s\", \"pv\":%d, \"attaque\":%d, \"defense\":%d, \"image\":\"%s\"}",
                id_client, nom, pv, atk, def, imagePath
        );
    }

    // Creates the JSON payload for the login process
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

    // Prepares a JSON string to request another player's inventory
    public static String buildGetOpponentInventoryRequest(String opponentUsername) {
        return String.format(
                "{\"username\": \"%s\"}",
                opponentUsername
        );
    }

    // Formats IDs into a JSON string for a card trade proposal
    public static String buildTradeRequestData(int initiatorId, int offeredCardId, int receiverId, int requestedCardId) {
        return String.format(
                "{\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_receiver\": %d, \"id_card_receiver\": %d}",
                initiatorId,
                offeredCardId,
                receiverId,
                requestedCardId
        );
    }

    // Extracts a list of Player objects from a JSON server response
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

    // Parses a JSON response to create a list of Card objects
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

    // Retrieves the user's inventory from the login confirmation JSON
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

    // Converts a trade notification JSON into a model object
    public static TradeRequestModel parseTradeRequestNotification(String json) {
        int initId = extractInt(json, "\"id_initiator\":");
        int initCard = extractInt(json, "\"id_card_initiator\":");
        int recvCard = extractInt(json, "\"id_card_receiver\":");

        return new TradeRequestModel(initId, initCard, recvCard);
    }

    // Generates the JSON response for accepting or refusing a trade
    public static String buildTradeResponseJson(boolean accepted, TradeRequestModel request, int receiverId) {
        return String.format(
                "{\"accepted\": %b, \"id_initiator\": %d, \"id_card_initiator\": %d, \"id_card_receiver\": %d, \"id_receiver\": %d}",
                accepted,
                request.getInitiatorId(),
                request.getInitiatorCardId(),
                request.getReceiverCardId(),
                receiverId
        );
    }

    // Wraps an action and data into a standard JSON response format
    public static String buildResponse(String actionName, String dataJson) {
        return String.format(
                "{\"type\":\"response\", \"nom\":\"%s\", \"data\":%s}",
                actionName,
                dataJson
        );
    }

    // Extracts the updated card list from a trade result message
    public static List<Card> parseInventoryFromTradeResult(String jsonResponse) {
        List<Card> cards = new ArrayList<>();

        // On cherche le tableau "hand": [...]
        int handIndex = jsonResponse.indexOf("\"hand\":");
        if (handIndex == -1) return cards;

        int startIndex = jsonResponse.indexOf("[", handIndex);
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

    // Finds and returns the username from the login response
    public static String parseUsernameFromLogin(String jsonResponse) {
        return extractString(jsonResponse, "\"username\":");
    }

    // Helper method to create a Card object from its JSON representation
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

    // Uses regex to find and parse an integer value from JSON
    private static int extractInt(String source, String key) {
        Pattern p = Pattern.compile(key + "\\s*(\\d+)");
        Matcher m = p.matcher(source);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    // Uses regex to extract a string value from a JSON field
    private static String extractString(String source, String key) {
        Pattern p = Pattern.compile(key + "\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        return m.find() ? m.group(1) : "Inconnu";
    }

    // Prepares JSON data to challenge another player to a combat
    public static String buildFightRequestData(int initiatorId, int cardInitiatorId, int receiverId, int cardReceiverId) {
        return String.format(
                "{\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_receiver\": %d, \"id_card_receiver\": %d}",
                initiatorId,
                cardInitiatorId,
                receiverId,
                cardReceiverId
        );
    }

    // Parses a fight proposal notification from a JSON string
    public static TradeRequestModel parseFightRequestNotification(String json) {

        TradeRequestModel req = parseTradeRequestNotification(json);
        req.setFight(true);
        return req;
    }

    // Formats the response JSON for a fight invitation
    public static String buildFightResponseJson(boolean accepted, TradeRequestModel request, int receiverId) {
        return String.format(
                "{\"accepted\": %b, \"id_initiator\": %d, \"id_card_initiator\": %d, \"id_card_receiver\": %d, \"id_receiver\": %d}",
                accepted,
                request.getInitiatorId(),
                request.getInitiatorCardId(),
                request.getReceiverCardId(),
                receiverId
        );
    }

    // Parses logs and opponent card details from a fight outcome
    public static FightResultModel parseFightResult(String json) {
        String log = extractString(json, "\"log\":");

        int oppIndex = json.indexOf("\"opponent_card\":");
        Card oppCard = null;

        if (oppIndex != -1) {
            int startIndex = json.indexOf("{", oppIndex);
            int endIndex = json.indexOf("}", startIndex);

            if (startIndex != -1 && endIndex != -1) {
                String cardJson = json.substring(startIndex, endIndex + 1);
                oppCard = extractCardFromJson(cardJson);
            }
        }

        return new FightResultModel(log, oppCard);
    }
}
package app.service;

import app.model.Card;
import app.model.FightResultModel;
import app.model.Player;
import app.model.TradeRequestModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for building and parsing JSON strings used in network communication.
 * This class provides static methods to format requests/responses and extract model
 * objects from server messages using regular expressions.
 */
public class JsonUtils {

    /**
     * Wraps action data into a standard request JSON structure.
     * @param actionName The name of the request action.
     * @param dataJson The JSON data associated with the action.
     * @return A formatted request JSON string.
     */
    public static String buildRequest(String actionName, String dataJson) {
        return String.format(
                "{\"type\":\"request\", \"nom\":\"%s\", \"data\":%s}",
                actionName,
                dataJson
        );
    }

    /**
     * Builds the JSON data payload for a card creation request.
     * @param id_client The unique ID of the client creating the card.
     * @param nom The name of the card.
     * @param pv Health points.
     * @param atk Attack points.
     * @param def Defense points.
     * @param imagePath The path to the card image.
     * @return A formatted JSON string for card creation.
     */
    public static String buildCardCreationData(int id_client, String nom, int pv, int atk, int def, String imagePath) {
        return String.format(
                "{\"id_client\":\"%d\", \"nomCarte\":\"%s\", \"pv\":%d, \"attaque\":%d, \"defense\":%d, \"image\":\"%s\"}",
                id_client, nom, pv, atk, def, imagePath
        );
    }

    /**
     * Builds login data for authentication.
     * @param idClient Existing client ID (if any).
     * @param username Chosen username.
     * @return A JSON string representing login credentials.
     */
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

    /**
     * Formats a request to retrieve a specific player's inventory.
     * @param opponentUsername The username of the target player.
     * @return A JSON payload for inventory lookup.
     */
    public static String buildGetOpponentInventoryRequest(String opponentUsername) {
        return String.format(
                "{\"username\": \"%s\"}",
                opponentUsername
        );
    }

    /**
     * Builds the data payload for initiating a trade request.
     * @param initiatorId The ID of the requesting player.
     * @param offeredCardId The ID of the card being offered.
     * @param receiverId The ID of the target player.
     * @param requestedCardId The ID of the card being requested.
     * @return A JSON payload for trade initialization.
     */
    public static String buildTradeRequestData(int initiatorId, int offeredCardId, int receiverId, int requestedCardId) {
        return String.format(
                "{\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_receiver\": %d, \"id_card_receiver\": %d}",
                initiatorId,
                offeredCardId,
                receiverId,
                requestedCardId
        );
    }

    /**
     * Parses a list of connected players from a server response.
     * @param jsonResponse The raw JSON response from the server.
     * @return A list of Player objects currently connected.
     */
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

    /**
     * Parses an opponent's inventory from a JSON response.
     * @param jsonResponse JSON response containing card data.
     * @return A list of Card objects found in the inventory.
     */
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

    /**
     * Parses the main inventory received during the login process.
     * @param jsonResponse Raw login response.
     * @return A list of cards owned by the logged-in player.
     */
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

    /**
     * Parses a trade request notification from the server.
     * @param json Raw notification JSON.
     * @return A TradeRequestModel containing the request details.
     */
    public static TradeRequestModel parseTradeRequestNotification(String json) {
        int initId = extractInt(json, "\"id_initiator\":");
        int initCard = extractInt(json, "\"id_card_initiator\":");
        int recvCard = extractInt(json, "\"id_card_receiver\":");

        return new TradeRequestModel(initId, initCard, recvCard);
    }

    /**
     * Builds the JSON response for a trade confirmation.
     * @param accepted Decision of the receiver.
     * @param request The original trade request.
     * @param receiverId The ID of the player responding.
     * @return A JSON payload to answer a trade request.
     */
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

    /**
     * Wraps action data into a standard response JSON structure.
     * @param actionName The name of the response action.
     * @param dataJson The data payload.
     * @return A formatted response JSON string.
     */
    public static String buildResponse(String actionName, String dataJson) {
        return String.format(
                "{\"type\":\"response\", \"nom\":\"%s\", \"data\":%s}",
                actionName,
                dataJson
        );
    }

    /**
     * Extracts updated hand information from a trade result response.
     * @param jsonResponse Server response after a completed trade.
     * @return The new list of cards in hand.
     */
    public static List<Card> parseInventoryFromTradeResult(String jsonResponse) {
        List<Card> cards = new ArrayList<>();

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

    /**
     * Extracts the username from a login response.
     * @param jsonResponse Raw login response.
     * @return The extracted username string.
     */
    public static String parseUsernameFromLogin(String jsonResponse) {
        return extractString(jsonResponse, "\"username\":");
    }

    /**
     * Internal method to create a Card object from a JSON card representation.
     * @param cardJson Partial JSON string for a card.
     * @return A Card instance populated with the extracted data.
     */
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

    /**
     * Helper to extract an integer value for a given key.
     * @param source The JSON source string.
     * @param key The key to look for.
     * @return The parsed integer, or 0 if not found.
     */
    private static int extractInt(String source, String key) {
        Pattern p = Pattern.compile(key + "\\s*(\\d+)");
        Matcher m = p.matcher(source);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    /**
     * Helper to extract a string value for a given key.
     * @param source The JSON source string.
     * @param key The key to look for.
     * @return The parsed string value.
     */
    private static String extractString(String source, String key) {
        Pattern p = Pattern.compile(key + "\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(source);
        return m.find() ? m.group(1) : "Inconnu";
    }

    /**
     * Builds data payload for initiating a fight challenge.
     * @param initiatorId Challenging player ID.
     * @param cardInitiatorId Challenging card ID.
     * @param receiverId Opponent player ID.
     * @param cardReceiverId Targeted card ID.
     * @return A fight request JSON payload.
     */
    public static String buildFightRequestData(int initiatorId, int cardInitiatorId, int receiverId, int cardReceiverId) {
        return String.format(
                "{\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_receiver\": %d, \"id_card_receiver\": %d}",
                initiatorId,
                cardInitiatorId,
                receiverId,
                cardReceiverId
        );
    }

    /**
     * Parses a fight request notification.
     * @param json Raw notification JSON.
     * @return A TradeRequestModel configured as a fight challenge.
     */
    public static TradeRequestModel parseFightRequestNotification(String json) {

        TradeRequestModel req = parseTradeRequestNotification(json);
        req.setFight(true);
        return req;
    }

    /**
     * Builds the JSON response for a fight confirmation.
     * @param accepted Decision to fight or flee.
     * @param request Original fight request.
     * @param receiverId Responding player ID.
     * @return A JSON response payload for a combat challenge.
     */
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

    /**
     * Parses the result of a battle from a JSON message.
     * @param json Raw result JSON.
     * @return A FightResultModel containing logs and opponent card state.
     */
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
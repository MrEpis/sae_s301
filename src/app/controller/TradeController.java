package app.controller;

import app.model.Card;
import app.model.Player;
import app.service.JsonUtils;
import app.service.NetworkService;
import app.views.TradeView;

import java.util.List;

public class TradeController {

    private final MainController mainController;
    private final Player currentPlayer;
    private final TradeView tradeView;

    public TradeController(MainController mainController, Player currentPlayer, TradeView tradeView) {
        this.mainController = mainController;
        this.currentPlayer = currentPlayer;
        this.tradeView = tradeView;
    }

    public void backToMenu() {
        mainController.showMenu();
    }

    public List<Card> getLocalPlayerInventory() {
        return currentPlayer.getInventory();
    }

    public void refreshPlayerList() {
        System.out.println("Demande de la liste des joueurs...");
        String request = JsonUtils.buildRequest("GET_CONNECTED_USERS", "{}");
        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            String response = net.sendRequest(request);
            if (response != null && response.contains("OK")) {
                List<String> players = JsonUtils.parsePlayerList(response);
                if (currentPlayer != null && currentPlayer.getName() != null) {
                    players.remove(currentPlayer.getName());
                }
                tradeView.updatePlayerList(players);
                tradeView.displayStatus(players.size() + " joueur(s) trouvé(s) (hors vous).");
            } else {
                tradeView.displayStatus("Erreur lors de la récupération des joueurs.");
            }
        }
    }

    public void loadOpponentInventory(String opponentName) {
        System.out.println("Demande inventaire pour : " + opponentName);
        tradeView.displayStatus("Chargement de l'inventaire de " + opponentName + "...");

        String dataJson = JsonUtils.buildGetOpponentInventoryRequest(opponentName);
        String request = JsonUtils.buildRequest("GET_OPPONENT_INVENTORY", dataJson);

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            String response = net.sendRequest(request);
            System.out.println("Réponse inventaire adverse : " + response);

            if (response != null && response.contains("OK")) {
                List<Card> opponentCards = JsonUtils.parseOpponentInventory(response);
                tradeView.updateOpponentInventory(opponentCards);
                tradeView.displayStatus("Inventaire de " + opponentName + " chargé (" + opponentCards.size() + " cartes).");
            } else {
                tradeView.displayStatus("Erreur chargement inventaire.");
            }
        }
    }

    public void sendTradeRequest(String opponentName, int offeredCardId, int requestedCardId) {
        System.out.println("Envoi de TradeRequest au serveur pour " + opponentName);
        tradeView.displayStatus("Envoi de la demande d'échange...");

        String dataJson = JsonUtils.buildTradeRequestData(offeredCardId, requestedCardId, opponentName);

        String request = JsonUtils.buildRequest("TradeRequest", dataJson);

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            String response = net.sendRequest(request);
            System.out.println("Réponse Trade : " + response);

            if (response != null && response.contains("OK")) {
                // Succès : La demande a été reçue par le serveur et transmise à l'adversaire
                tradeView.displayStatus("Demande envoyée avec succès à " + opponentName + " ! En attente...");
            } else {
                // Échec (serveur renvoie ERROR ou pas de réponse)
                tradeView.displayStatus("Erreur : La demande d'échange a échoué.");
            }
        } else {
            tradeView.displayStatus("Erreur critique : Pas de connexion réseau.");
        }
    }
}
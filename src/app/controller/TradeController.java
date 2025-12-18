package app.controller;

import app.model.Card;
import app.model.Player;
import app.service.JsonUtils;
import app.service.NetworkService;
import app.views.TradeView;

import java.util.ArrayList;
import java.util.List;

public class TradeController {

    private final MainController mainController;
    private final Player currentPlayer;
    private final TradeView tradeView;

    private List<Player> connectedPlayers = new ArrayList<>();

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
                List<Player> allPlayers = JsonUtils.parsePlayerList(response);

                this.connectedPlayers.clear();
                List<String> playerNamesForView = new ArrayList<>();

                for (Player p : allPlayers) {
                    if (p.getId_Client() != currentPlayer.getId_Client()) {
                        this.connectedPlayers.add(p);
                        playerNamesForView.add(p.getName());
                    }
                }

                tradeView.updatePlayerList(playerNamesForView);
                tradeView.displayStatus(connectedPlayers.size() + " joueur(s) trouvé(s) (hors vous).");
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

        int opponentId = -1;
        for (Player p : connectedPlayers) {
            if (p.getName().equals(opponentName)) {
                opponentId = p.getId_Client();
                break;
            }
        }

        if (opponentId == -1) {
            tradeView.displayStatus("Erreur : Impossible de trouver l'ID du joueur " + opponentName);
            return;
        }

        mainController.setPendingTradeOpponent(opponentName);

        tradeView.displayStatus("Envoi de la demande d'échange à " + opponentName + " (ID: " + opponentId + ")...");

        String dataJson = JsonUtils.buildTradeRequestData(
                currentPlayer.getId_Client(),
                offeredCardId,
                opponentId,
                requestedCardId
        );

        String request = JsonUtils.buildRequest("TradeRequest", dataJson);

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            String response = net.sendRequest(request);
            System.out.println("Réponse Trade : " + response);

            if (response != null && response.contains("OK")) {
                tradeView.displayStatus("Demande envoyée avec succès ! En attente...");
            } else {
                tradeView.displayStatus("Erreur : La demande d'échange a échoué.");
            }
        } else {
            tradeView.displayStatus("Erreur critique : Pas de connexion réseau.");
        }
    }

    public Player getLocalPlayer() {
        return mainController.getLocalPlayer();
    }
}
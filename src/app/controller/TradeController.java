package app.controller;

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

    // NOUVEAU : Récupère la liste des joueurs connectés
    public void refreshPlayerList() {
        System.out.println("Demande de la liste des joueurs...");

        String request = JsonUtils.buildRequest("GET_CONNECTED_USERS", "{}");

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            String response = net.sendRequest(request);

            if (response != null && response.contains("OK")) {
                List<String> players = JsonUtils.parsePlayerList(response);

                players.remove(currentPlayer.getName());
                tradeView.updatePlayerList(players);
                tradeView.displayStatus(players.size() + " joueur(s) trouvé(s).");
            } else {
                tradeView.displayStatus("Erreur lors de la récupération des joueurs.");
            }
        }
    }

    public void searchOpponent(String opponentName) {
        System.out.println("Recherche de l'adversaire : " + opponentName);
        // TODO: C'est ici qu'on demandera l'inventaire de l'adversaire plus tard
        tradeView.displayStatus("Adversaire " + opponentName + " sélectionné.");
    }

    public void sendTradeRequest(String opponentName, int offeredCardId, int requestedCardId) {
        System.out.println("Envoi de TradeRequest au serveur pour " + opponentName);
        tradeView.displayStatus("Requête d'échange envoyée à " + opponentName);
    }
}
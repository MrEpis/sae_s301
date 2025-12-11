package app.controller;

import app.model.Player;
import app.views.TradeView;

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

    public void searchOpponent(String opponentName) {
        System.out.println("Recherche simulée de l'adversaire : " + opponentName);
        tradeView.displayStatus("Adversaire " + opponentName + " trouvé.");
    }

    // RETOUR AUX IDs (int)
    public void sendTradeRequest(String opponentName, int offeredCardId, int requestedCardId) {

        System.out.println("Envoi de TradeRequest au serveur...");

        // JSON : { "type": "request", "nom": "TradeRequest", "data": { "carteA": 1, "carteB": 2, "adversaire": "Bob" } }
        // TODO: Utiliser JsonUtils et NetworkService ici

        tradeView.displayStatus("Requête d'échange (ID " + offeredCardId + " vs ID " + requestedCardId + ") envoyée à " + opponentName);
    }
}
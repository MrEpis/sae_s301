package java.controller;

import java.model.Player;

import java.views.TradeView;

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

    public void sendTradeRequest(String opponentName, int playerCardId, int opponentCardId) {
        System.out.println("Requête d'échange envoyée à " + opponentName +
                " (Offre: " + playerCardId + ", Demande: " + opponentCardId + ").");

    }

    // TODO: Ajouter une méthode pour rechercher les cartes de l'adversaire.
    // TODO: Ajouter une méthode pour valider la sélection des cartes.
}
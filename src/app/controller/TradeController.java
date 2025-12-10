package app.controller;

import app.model.Player;

import app.views.TradeView;

public class TradeController {

    private final MainController mainController;
    private final Player currentPlayer;
    private final TradeView tradeView;

    // TODO: Ajouter NetworkService et CardList lorsque ces classes seront prêtes.

    public TradeController(MainController mainController, Player currentPlayer, TradeView tradeView) {
        this.mainController = mainController;
        this.currentPlayer = currentPlayer;
        this.tradeView = tradeView;

        System.out.println("TradeController initialisé pour le joueur : " + currentPlayer.getName());
    }

    public void backToMenu() {
        mainController.showMenu();
    }

    public void searchOpponent(String opponentName) {
        // String response = networkService.sendRequest("SEARCH_PLAYER:" + opponentName);

        System.out.println("Recherche simulée de l'adversaire : " + opponentName);

        // TODO: Mettre à jour la TradeView avec l'inventaire de l'adversaire trouvé
        tradeView.displayStatus("Adversaire " + opponentName + " trouvé. Sélectionnez les cartes.");
    }


    public void sendTradeRequest(String opponentName, String offeredCardName, String requestedCardName) {

        System.out.println("Envoi de TradeRequest au serveur...");

        // TODO: Implémenter la logique NetworkService.sendRequest() ici.

        tradeView.displayStatus("Requête d'échange envoyée à " + opponentName + ". En attente de réponse...");
    }
}
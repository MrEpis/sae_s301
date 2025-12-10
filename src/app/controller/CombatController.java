package app.controller;

import app.model.Card;
import app.model.Game;
import app.model.Player;
import app.views.CombatView;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;

public class CombatController {

    private final MainController mainController;
    private final Player playerLocal;
    private Player playerAdversaire;
    private Game currentGame;
    private CombatView combatView;

    public CombatController(MainController mainController, Player player) {
        this.mainController = mainController;
        this.playerLocal = player;

        this.playerAdversaire = new Player(999, "Adversaire IA");

        if (this.playerLocal.getInventory().isEmpty()) {
            this.playerLocal.getInventory().add(new Card("Dragon Bleu", 20, 5, 10));
            this.playerLocal.getInventory().add(new Card("Guerrier", 15, 3, 8));
        }
        if (this.playerAdversaire.getInventory().isEmpty()) {
            this.playerAdversaire.getInventory().add(new Card("Golem", 30, 8, 7));
            this.playerAdversaire.getInventory().add(new Card("Voleur", 10, 2, 12));
        }
    }

    public void setView(CombatView view) {
        this.combatView = view;
    }

    public ListView<Card> getPlayerInventory() {
        ListView<Card> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(playerLocal.getInventory()));
        return listView;
    }

    public ListView<Card> getOpponentInventory() {
        ListView<Card> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(playerAdversaire.getInventory()));
        return listView;
    }

    public void searchOpponent(String name) {
        System.out.println("Recherche de l'adversaire : " + name);
        this.playerAdversaire = new Player(202, name);
        this.playerAdversaire.getInventory().add(new Card("Monstre de " + name, 25, 5, 5));

        // TODO: Mettre à jour la vue avec le nouvel inventaire
    }

    public void setupDuel(Card cardPlayer, Card cardOpponent) {
        Card c1 = new Card(cardPlayer.getNom(), cardPlayer.getHp(), cardPlayer.getDef(), cardPlayer.getAtk());
        Card c2 = new Card(cardOpponent.getNom(), cardOpponent.getHp(), cardOpponent.getDef(), cardOpponent.getAtk());

        this.currentGame = new Game(playerLocal, playerAdversaire, c1, c2);

    }

    public void launchInstantFight() {
        if (currentGame == null) return;

        Card carte1 = currentGame.getCardPlayer1();
        Card carte2 = currentGame.getCardPlayer2();

        int score1 = carte1.getHp() + carte1.getAtk() - carte2.getDef();
        int score2 = carte2.getHp() + carte2.getAtk() - carte1.getDef();

        String resultMessage;

        if (score1 > score2) {
            resultMessage = playerLocal.getName() + " GAGNE !";
            carte2.setHp(0);
        } else if (score2 > score1) {
            resultMessage = playerAdversaire.getName() + " GAGNE !";
            carte1.setHp(0);
        } else {
            resultMessage = "ÉGALITÉ !";
        }

        // TODO: Appeler une méthode de la vue pour mettre à jour l'affichage si la référence est stockée
    }

    public void backToMenu() {
        mainController.showMenu();
    }
}
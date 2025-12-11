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

        // Mise à jour des Mocks avec ID et ImagePath (null pour l'instant)
        if (this.playerLocal.getInventory().isEmpty()) {
            this.playerLocal.getInventory().add(new Card(1, "Dragon Bleu", 20, 5, 10, null));
            this.playerLocal.getInventory().add(new Card(2, "Guerrier", 15, 3, 8, null));
        }
        if (this.playerAdversaire.getInventory().isEmpty()) {
            this.playerAdversaire.getInventory().add(new Card(3, "Golem", 30, 8, 7, null));
            this.playerAdversaire.getInventory().add(new Card(4, "Voleur", 10, 2, 12, null));
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
        // Ajout avec ID et image null
        this.playerAdversaire.getInventory().add(new Card(5, "Monstre de " + name, 25, 5, 5, null));
    }

    public void setupDuel(Card cardPlayer, Card cardOpponent) {
        // Recréation des cartes pour le duel avec leurs propriétés
        Card c1 = new Card(cardPlayer.getId(), cardPlayer.getNom(), cardPlayer.getHp(), cardPlayer.getDef(), cardPlayer.getAtk(), cardPlayer.getImagePath());
        Card c2 = new Card(cardOpponent.getId(), cardOpponent.getNom(), cardOpponent.getHp(), cardOpponent.getDef(), cardOpponent.getAtk(), cardOpponent.getImagePath());

        this.currentGame = new Game(playerLocal, playerAdversaire, c1, c2);
    }

    public void launchInstantFight() {
        if (currentGame == null) return;

        Card carte1 = currentGame.getCardPlayer1();
        Card carte2 = currentGame.getCardPlayer2();

        int score1 = carte1.getHp() + carte1.getAtk() - carte2.getDef();
        int score2 = carte2.getHp() + carte2.getAtk() - carte1.getDef();

        if (score1 > score2) {
            System.out.println(playerLocal.getName() + " GAGNE !");
            carte2.setHp(0);
        } else if (score2 > score1) {
            System.out.println(playerAdversaire.getName() + " GAGNE !");
            carte1.setHp(0);
        } else {
            System.out.println("ÉGALITÉ !");
        }
    }

    public void backToMenu() {
        mainController.showMenu();
    }
}
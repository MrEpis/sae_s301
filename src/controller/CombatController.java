package controller;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.Card;
import model.Game;
import model.Player;
import views.CombatView;
import java.util.Arrays;
import java.util.List;

public class CombatController {

    private final Stage primaryStage;
    private final MainController mainController;
    private final CombatView combatView;
    private Game currentGame;

    private Player playerLocal = new Player(1, "Joueur LOCAL");
    private Player playerAdversaire = new Player(2, "Adversaire IA");

    public CombatController(Stage primaryStage, MainController mainController) {
        this.mainController = mainController;
        this.primaryStage = primaryStage;

        this.combatView = new CombatView(primaryStage, this);
    }

    public void showView() {
        primaryStage.setScene(combatView.createScene());
        primaryStage.setTitle("Combat - Sélection");
        primaryStage.show();
    }

    public ListView<Card> getPlayerInventory() {
        List<Card> localInventory = Arrays.asList(
                new Card("Dragon Bleu", 20, 5, 10),
                new Card("Guerrier", 15, 3, 8)
        );
        ListView<Card> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(localInventory));
        return listView;
    }

    public ListView<Card> getOpponentInventory() {
        List<Card> opponentInventory = Arrays.asList(
                new Card("Golem de Fer", 30, 8, 7),
                new Card("Voleur d'Ombre", 10, 2, 12)
        );
        ListView<Card> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(opponentInventory));
        return listView;
    }


    public void setupDuel(Card cardPlayer, Card cardOpponent) {
        Card card1 = new Card(cardPlayer.getNom(), cardPlayer.getHp(), cardPlayer.getDef(), cardPlayer.getAtk());
        Card card2 = new Card(cardOpponent.getNom(), cardOpponent.getHp(), cardOpponent.getDef(), cardOpponent.getAtk());

        this.currentGame = new Game(playerLocal, playerAdversaire, card1, card2);

        combatView.showDuelView(card1, card2);
        primaryStage.setTitle("Combat - Duel en Cours");
    }

    public void launchInstantFight() {
        Card carte1 = currentGame.getCardPlayer1();
        Card carte2 = currentGame.getCardPlayer2();

        int score1 = carte1.getHp() + carte1.getAtk() - carte2.getDef();
        int score2 = carte2.getHp() + carte2.getAtk() - carte1.getDef();

        String resultMessage;
        Player winner = null;

        if (score1 > score2) {
            resultMessage = playerLocal.getName() + " GAGNE ! (" + carte1.getNom() + ")";
            carte2.setHp(0);
            winner = playerLocal;
        } else if (score2 > score1) {
            resultMessage = playerAdversaire.getName() + " GAGNE ! (" + carte2.getNom() + ")";
            carte1.setHp(0);
            winner = playerAdversaire;
        } else {
            resultMessage = "ÉGALITÉ ! Personne ne perd de PV.";
        }

        currentGame.setWinner(winner);

        combatView.updateDuelDisplay(carte1, carte2, resultMessage);
    }

    public void backToMenu() {
        mainController.showMenu();
    }
}
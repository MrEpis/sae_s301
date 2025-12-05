package controller;

import model.Card;
import model.Player;
import views.CardCreationView;

public class CardCreationController {

    private final MainController mainController;
    private final Player player;
    private final CardCreationView creationView;

    public CardCreationController(MainController mainController, Player player, CardCreationView creationView) {
        this.mainController = mainController;
        this.player = player;
        this.creationView = creationView;
    }

    public void backToMenu() {
        mainController.showMenu();
    }

    public void saveCard(String name, int hp, int atk, int def) {
        if ((hp + atk + def) > CardCreationView.MAX_POINTS) {
            System.err.println("Erreur: Total de points supérieur au maximum autorisé.");
            return;
        }

        Card newCard = new Card(name, hp, atk, def);
        player.addCard(newCard);

        System.out.println("Carte '" + name + "' créée et ajoutée à l'inventaire.");

        backToMenu();
    }

    // TODO: Ajouter une méthode pour gérer la sélection d'image.
}
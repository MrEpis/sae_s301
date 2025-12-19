package app.controller;

import app.model.Player;

// Provides data access for the inventory display
public class InventoryController {

    private final MainController mainController;
    private final Player playerLocal;

    public InventoryController(MainController mainController, Player player) {
        this.mainController = mainController;
        this.playerLocal = player;
    }

    public void backToMenu() {
        mainController.showMenu();
    }

    public Player getPlayer() {
        return playerLocal;
    }

    public Player getLocalPlayer() {
        return mainController.getLocalPlayer();
    }
}
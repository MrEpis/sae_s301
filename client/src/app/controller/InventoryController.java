package app.controller;

import app.model.Player;

/**
 * Controller responsible for managing the local player's inventory view.
 * It provides access to the player's card collection and handles navigation back to the main menu.
 */
public class InventoryController {

    private final MainController mainController;
    private final Player playerLocal;

    /**
     * Initializes the InventoryController with the main controller and the local player.
     * @param mainController The application's main controller for navigation purposes.
     * @param player The local player whose inventory is being displayed.
     */
    public InventoryController(MainController mainController, Player player) {
        this.mainController = mainController;
        this.playerLocal = player;
    }

    /**
     * Navigates the application back to the main menu.
     */
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
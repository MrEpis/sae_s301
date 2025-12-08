package app.controller;

import app.model.Player;
import app.views.InventoryView;

public class InventoryController {

    private final MainController mainController;
    private final Player player;
    private final InventoryView inventoryView;

    public InventoryController(MainController mainController, Player player, InventoryView inventoryView) {
        this.mainController = mainController;
        this.player = player;
        this.inventoryView = inventoryView;
    }

    public void backToMenu() {
        mainController.showMenu();
    }

    // TODO: Ajouter des méthodes pour afficher les détails d'une carte sélectionnée,
}
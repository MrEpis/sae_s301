package app.controller;

import javafx.stage.Stage;
import app.views.*;
import app.model.Player;

public class MainController {

    private final Stage primaryStage;
    private final Player localPlayer;

    private CombatController combatController;
    private CardCreationController cardCreationController;
    private InventoryController inventoryController;
    private TradeController tradeController;

    private CardCreationView cardCreationView;
    private MenuView menuView;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.localPlayer = new Player(101, "Joueur Local");

        this.menuView = new MenuView(primaryStage, this);
        this.cardCreationView = new CardCreationView(primaryStage);

        this.cardCreationController = new CardCreationController(this, localPlayer, cardCreationView);
        this.cardCreationView.setController(this.cardCreationController);
    }

    public void start() {
        showMenu();
    }

    public void showMenu() {
        menuView.show();
    }

    public void showCombat() {
        this.combatController = new CombatController(this, localPlayer);
        CombatView combatView = new CombatView(primaryStage, this.combatController);
        combatView.show();
    }

    public void showInventory() {
        this.inventoryController = new InventoryController(this, localPlayer);
        InventoryView inventoryView = new InventoryView(primaryStage, this.inventoryController);
        inventoryView.show();
    }

    public void showCardCreation() {
        cardCreationView.show();
    }

    public void showTrade() {
        TradeView tradeView = new TradeView(primaryStage);
        this.tradeController = new TradeController(this, localPlayer, tradeView);
        tradeView.setController(this.tradeController);
        tradeView.show();
    }

    public void quit() {
        primaryStage.close();
    }
}
package controller;

import javafx.stage.Stage;
import views.MenuView;
import views.CombatView;
import views.InventoryView;
import views.CardCreationView;
import views.TradeView;

public class MainController {

    private final Stage primaryStage;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    public void showMenu() {
        new MenuView(primaryStage, this).show();
    }

    public void showCombat() {
        CombatController combatController = new CombatController(primaryStage, this);
        combatController.showView();
    }

    public void showInventory() {
        new InventoryView(primaryStage, this).show();
    }

    public void showCardCreation() {
        new CardCreationView(primaryStage, this).show();
    }

    public void showTrade() {
        new TradeView(primaryStage, this).show();
    }

    public void quit() {
        primaryStage.close();
    }
}
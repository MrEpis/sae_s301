package java.controller;

import javafx.stage.Stage;
import java.views.MenuView;
import java.views.InventoryView;
import java.views.CardCreationView;
import java.views.TradeView;
import java.model.Player; // Ajout pour la dépendance Player

public class MainController {

    private final Stage primaryStage;
    private final Player localPlayer = new Player(1, "LocalPlayer");

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
        CardCreationView cardCreationView = new CardCreationView(primaryStage);
        CardCreationController cardCreationController = new CardCreationController(
                this,
                localPlayer,
                cardCreationView
        );

        cardCreationView.setController(cardCreationController);
        cardCreationView.show();
    }

    public void showTrade() {
        new TradeView(primaryStage, this).show();
    }

    public void quit() {
        primaryStage.close();
    }
}
package views;

import controller.CombatController;
import javafx.geometry.Insets; // Ajouté Insets
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import model.Card;

public class CombatView {

    private final Stage primaryStage;
    private final CombatController controller;
    private VBox centerContainer;
    private VBox playerCardDisplay;
    private VBox opponentCardDisplay;
    private Label statusLabel;

    private Card selectedPlayerCard;
    private Card selectedOpponentCard;

    public CombatView(Stage primaryStage, CombatController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #2e2e2e;");

        Label titleLabel = createLabel("Card Duel Setup", 28, "#ffffff", FontWeight.BOLD);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER);
        root.setCenter(centerContainer);

        showSelectionView();

        // Bottom: Back Button
        Button backButton = createActionButton("Retour au Menu", "#666666");
        backButton.setOnAction(e -> controller.backToMenu());

        VBox bottomBox = new VBox(backButton);
        bottomBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(bottomBox, new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);

        return new Scene(root, 1000, 700);
    }


    private void showSelectionView() {
        centerContainer.getChildren().clear();

        VBox selectionBox = new VBox(15);
        selectionBox.setAlignment(Pos.CENTER);

        HBox inventorySelection = new HBox(30);
        inventorySelection.setAlignment(Pos.CENTER);

        VBox playerPanel = createInventoryPanel("Votre Inventaire", "#4CAF50", controller.getPlayerInventory(), true);
        VBox opponentPanel = createInventoryPanel("Cartes Adversaires (Exemple)", "#F44336", controller.getOpponentInventory(), false);

        inventorySelection.getChildren().addAll(playerPanel, opponentPanel);

        Button startSetupButton = createActionButton("Commencer le Duel", "#2196F3");
        startSetupButton.setOnAction(e -> {
            if (selectedPlayerCard != null && selectedOpponentCard != null) {
                controller.setupDuel(selectedPlayerCard, selectedOpponentCard);
            } else {
                System.err.println("Veuillez sélectionner une carte pour chaque joueur.");
            }
        });

        selectionBox.getChildren().addAll(createLabel("Sélectionnez vos cartes", 18, "#FFC107", FontWeight.NORMAL), inventorySelection, startSetupButton);
        centerContainer.getChildren().add(selectionBox);
    }

    // Creates a list panel for selecting cards from an inventory
    private VBox createInventoryPanel(String title, String color, ListView<Card> cardListView, boolean isPlayer) {
        VBox panel = new VBox(10);
        panel.setPrefWidth(400);
        panel.setPrefHeight(450);
        panel.setStyle("-fx-background-color: #4a4a4a; -fx-border-color: " + color + "; -fx-border-width: 2;");

        cardListView.setCellFactory(list -> new CardListCell());
        cardListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (isPlayer) {
                selectedPlayerCard = newV;
            } else {
                selectedOpponentCard = newV;
            }
        });

        panel.getChildren().addAll(createLabel(title, 18, "#ffffff", FontWeight.BOLD), cardListView);
        return panel;
    }

    public void showDuelView(Card playerCard, Card opponentCard) {
        centerContainer.getChildren().clear();

        VBox duelBox = new VBox(20);
        duelBox.setAlignment(Pos.CENTER);

        statusLabel = createLabel("Prêt au Combat !", 18, "#ffffff", FontWeight.NORMAL);

        HBox cardArea = new HBox(50);
        cardArea.setAlignment(Pos.CENTER);

        playerCardDisplay = createCardDisplay(playerCard, "#008b00");
        Label vsLabel = createLabel("VS", 40, "#FFC107", FontWeight.EXTRA_BOLD);
        opponentCardDisplay = createCardDisplay(opponentCard, "#8b0000");

        cardArea.getChildren().addAll(playerCardDisplay, vsLabel, opponentCardDisplay);

        Button startCombatButton = createActionButton("Lancer le Combat INSTANTANÉ", "#4CAF50");
        startCombatButton.setOnAction(e -> controller.launchInstantFight());

        duelBox.getChildren().addAll(statusLabel, cardArea, startCombatButton);
        centerContainer.getChildren().add(duelBox);
    }

    public void updateDuelDisplay(Card playerCard, Card opponentCard, String result) {
        playerCardDisplay = createCardDisplay(playerCard, "#008b00");
        opponentCardDisplay = createCardDisplay(opponentCard, "#8b0000");

        HBox cardArea = (HBox) ((VBox) centerContainer.getChildren().get(0)).getChildren().get(2); // Accès par index
        cardArea.getChildren().set(0, playerCardDisplay);
        cardArea.getChildren().set(2, opponentCardDisplay);

        statusLabel.setText(result);
    }

    private VBox createCardDisplay(Card card, String borderColor) {
        VBox cardSlot = new VBox(10);
        cardSlot.setPrefSize(200, 280);
        cardSlot.setAlignment(Pos.TOP_CENTER);

        cardSlot.setStyle(
                "-fx-background-color: #333333; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 4; " +
                        "-fx-border-style: solid;"
        );

        VBox stats = new VBox(5);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setPadding(new Insets(0, 0, 0, 5));

        stats.getChildren().addAll(
                createLabel("HP: " + card.getHp(), 14, "#4CAF50", FontWeight.BOLD), // Affiche les PV actuels
                createLabel("ATK: " + card.getAtk(), 14, "#F44336", FontWeight.BOLD),
                createLabel("DEF: " + card.getDef(), 14, "#2196F3", FontWeight.BOLD)
        );

        if (card.getHp() <= 0) {
            Label defeated = createLabel("VAINCU", 24, "#FF0000", FontWeight.EXTRA_BOLD);
            VBox.setMargin(defeated, new Insets(10, 0, 0, 0));
            stats.getChildren().add(defeated);
        }

        cardSlot.getChildren().addAll(createLabel(card.getNom(), 16, "#ffffff", FontWeight.BOLD), stats);
        return cardSlot;
    }

    static class CardListCell extends javafx.scene.control.ListCell<Card> {
        @Override
        protected void updateItem(Card item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                setText(item.getNom() + " (ATK: " + item.getAtk() + ")");
            }
        }
    }


    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Card Duel");
        primaryStage.show();
    }

    private Label createLabel(String text, int size, String color, FontWeight weight) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", weight, size));
        label.setTextFill(Color.web(color));
        return label;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(250, 45);
        btn.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: black; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold;"
        );
        return btn;
    }
}
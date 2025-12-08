package java.views;

import java.controller.CombatController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.model.Card;

public class CombatView {

    private final Stage primaryStage;
    private final CombatController controller;
    private VBox centerContainer;
    private VBox playerCardDisplay;
    private VBox opponentCardDisplay;
    private Label statusLabel;
    private Card selectedPlayerCard;
    private Card selectedOpponentCard;

    private TextField opponentNameField;
    private HBox inventorySelection;

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

        centerContainer = new VBox(20);
        centerContainer.setAlignment(Pos.CENTER);
        root.setCenter(centerContainer);

        showSelectionView();

        Button backButton = createActionButton("Retour au Menu", "#D9C6F0");
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle("-fx-background-color: #F1EBFA; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12pt;");
            backButton.setCursor(javafx.scene.Cursor.HAND);
        });

        backButton.setOnMouseExited(e -> {
            backButton.setStyle("-fx-background-color: #D9C6F0; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12pt;");
            backButton.setCursor(javafx.scene.Cursor.DEFAULT);
        });
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

        // Zone de sélection d'adversaire
        VBox opponentSelectionArea = createOpponentSelectionArea();

        // Zone d'inventaires
        inventorySelection = new HBox(30);
        inventorySelection.setAlignment(Pos.CENTER);

        // Inventaire du joueur
        VBox playerPanel = createInventoryPanel("Votre Inventaire", "#4CAF50", controller.getPlayerInventory(), true);
        inventorySelection.getChildren().add(playerPanel);

        // Inventaire de l'adversaire
        VBox opponentPanel = createInventoryPanel("Inventaire Adversaire", "#F44336", new ListView<Card>(), false);
        opponentPanel.setId("opponentPanel");
        inventorySelection.getChildren().add(opponentPanel);


        Button startSetupButton = createActionButton("Commencer le Duel", "#7834CB");
        startSetupButton.setOnMouseEntered(e -> {
            startSetupButton.setStyle("-fx-background-color: #9059D4; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12pt;");
            startSetupButton.setCursor(javafx.scene.Cursor.HAND);
        });

        startSetupButton.setOnMouseExited(e -> {
            startSetupButton.setStyle("-fx-background-color: #7834CB; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12pt;");
            startSetupButton.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        startSetupButton.setOnAction(e -> {
            if (selectedPlayerCard != null && selectedOpponentCard != null) {
                controller.setupDuel(selectedPlayerCard, selectedOpponentCard);
            } else {
                System.err.println("Veuillez sélectionner une carte pour chaque joueur.");
            }
        });

        selectionBox.getChildren().addAll(
                opponentSelectionArea,
                createLabel("Sélectionnez vos cartes", 18, "#FFC107", FontWeight.NORMAL),
                inventorySelection,
                startSetupButton
        );
        centerContainer.getChildren().add(selectionBox);
    }

    private VBox createOpponentSelectionArea() {
        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER);

        opponentNameField = new TextField();
        opponentNameField.setPromptText("Entrez le nom de l'adversaire");
        final String STYLE_NORMAL = "-fx-background-color: #A97DDE; -fx-text-fill: black; -fx-font-weight: bold;";
        final String STYLE_HOVER = "-fx-background-color: #9059D4; -fx-text-fill: black; -fx-font-weight: bold;";
        Button searchButton = createActionButton("Rechercher Adversaire", "#C5CC8F");
        searchButton.setStyle(STYLE_NORMAL);
        searchButton.setPrefSize(200, 30);

        searchButton.setOnMouseEntered(e -> {
            searchButton.setStyle(STYLE_HOVER);
            searchButton.setCursor(javafx.scene.Cursor.HAND);
        });

        searchButton.setOnMouseExited(e -> {
            searchButton.setStyle(STYLE_NORMAL);
            searchButton.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        searchButton.setOnAction(e -> {
            String name = opponentNameField.getText();
            // java.controller.searchOpponent(name);
        });

        inputRow.getChildren().addAll(opponentNameField, searchButton);

        VBox area = new VBox(5);
        area.setAlignment(Pos.CENTER);
        area.getChildren().addAll(createLabel("Choisir un Adversaire", 16, "#ffffff", FontWeight.BOLD), inputRow);

        return area;
    }

    public void updateOpponentInventory(String opponentName, ListView<Card> opponentInventory) {
        VBox oldOpponentPanel = (VBox) primaryStage.getScene().lookup("#opponentPanel");

        if (oldOpponentPanel != null) {
            VBox newOpponentPanel = createInventoryPanel("Inventaire de " + opponentName, "#F44336", opponentInventory, false);
            newOpponentPanel.setId("opponentPanel"); // Assurer que l'ID est conservé

            int index = inventorySelection.getChildren().indexOf(oldOpponentPanel);
            if (index != -1) {
                inventorySelection.getChildren().set(index, newOpponentPanel);
            }
        }
    }


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
        VBox newPlayerDisplay = createCardDisplay(playerCard, "#008b00");
        VBox newOpponentDisplay = createCardDisplay(opponentCard, "#8b0000");

        HBox cardArea = (HBox) ((VBox) centerContainer.getChildren().get(0)).getChildren().get(2);
        cardArea.getChildren().set(0, newPlayerDisplay);
        cardArea.getChildren().set(2, newOpponentDisplay);

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
                createLabel("HP: " + card.getHp(), 14, "#4CAF50", FontWeight.BOLD),
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
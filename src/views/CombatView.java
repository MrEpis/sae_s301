package views;

import javafx.geometry.Insets;
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

public class CombatView {

    private final Stage primaryStage;

    private VBox centerContainer;

    private String playerCardName = null;
    private String opponentCardName = null;

    public CombatView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2e2e2e;");

        Label titleLabel = createLabel("Card Duel Setup", 28, "#ffffff", FontWeight.BOLD);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        // Center Container will hold either selection or duel view
        centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER);
        root.setCenter(centerContainer);

        showSelectionView();

        // Bottom: Back Button
        Button backButton = createActionButton("Back to Menu", "#666666");
        backButton.setOnAction(e -> new MenuView(primaryStage).show());

        VBox bottomBox = new VBox(backButton);
        bottomBox.setAlignment(Pos.CENTER);
        root.setBottom(bottomBox);

        return new Scene(root, 1000, 700);
    }

    // Card Selection View

    private void showSelectionView() {
        centerContainer.getChildren().clear();

        VBox selectionBox = new VBox(15);
        selectionBox.setAlignment(Pos.CENTER);

        HBox inventorySelection = new HBox(30);
        inventorySelection.setAlignment(Pos.CENTER);

        VBox playerPanel = createInventoryPanel("Your Inventory", "#4CAF50", true); // Player's selection panel
        VBox opponentPanel = createInventoryPanel("Opponent's Available Cards", "#F44336", false); // Opponent's selection panel

        inventorySelection.getChildren().addAll(playerPanel, opponentPanel);

        Button startSetupButton = createActionButton("Commencer le Combat (Setup)", "#2196F3");
        startSetupButton.setOnAction(e -> showDuelView()); // Transition to Duel View

        selectionBox.getChildren().addAll(createLabel("Select Your Card and Opponent's Card", 18, "#FFC107", FontWeight.NORMAL), inventorySelection, startSetupButton);
        centerContainer.getChildren().add(selectionBox);
    }

    // Creates a list panel for selecting cards from an inventory
    private VBox createInventoryPanel(String title, String color, boolean isPlayer) {
        VBox panel = new VBox(10);
        panel.setPrefWidth(400);
        panel.setPrefHeight(450);
        panel.setStyle("-fx-background-color: #4a4a4a; -fx-border-color: " + color + "; -fx-border-width: 2;");

        ListView<String> cardListView = new ListView<>();

        // Placeholder data and selection logic
        if (isPlayer) {
            cardListView.getItems().addAll("Caillou", "Voiture", "Aspirateur");
            cardListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> playerCardName = newV);
        } else {
            cardListView.getItems().addAll("Feutre", "Bureau", "Chausette");
            cardListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> opponentCardName = newV);
        }

        panel.getChildren().addAll(createLabel(title, 18, "#ffffff", FontWeight.BOLD), cardListView);
        return panel;
    }

    // Duel View

    private void showDuelView() {
        centerContainer.getChildren().clear();

        VBox duelBox = new VBox(20);
        duelBox.setAlignment(Pos.CENTER);

        Label statusLabel = createLabel("Waiting to Start Duel", 18, "#ffffff", FontWeight.NORMAL);

        HBox cardArea = new HBox(50);
        cardArea.setAlignment(Pos.CENTER);

        VBox playerCardDisplay = createCardDisplay(playerCardName != null ? playerCardName : "Player Card", "#008b00");
        Label vsLabel = createLabel("VS", 40, "#FFC107", FontWeight.EXTRA_BOLD);
        VBox opponentCardDisplay = createCardDisplay(opponentCardName != null ? opponentCardName : "Opponent Card", "#8b0000");

        cardArea.getChildren().addAll(playerCardDisplay, vsLabel, opponentCardDisplay);

        Button startCombatButton = createActionButton("Lancer le Combat", "#4CAF50");
        startCombatButton.setOnAction(e -> statusLabel.setText("Combat Launched! (Logic to follow)"));

        duelBox.getChildren().addAll(statusLabel, cardArea, startCombatButton);
        centerContainer.getChildren().add(duelBox);
    }


    private VBox createCardDisplay(String cardName, String borderColor) {
        VBox cardSlot = new VBox(10);
        cardSlot.setPrefSize(200, 280);
        cardSlot.setAlignment(Pos.TOP_CENTER);

        cardSlot.setStyle(
                "-fx-background-color: #333333; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 4; " +
                        "-fx-border-style: solid;"
        );

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setPrefSize(180, 150);

        VBox stats = new VBox(5);

        cardSlot.getChildren().addAll(createLabel(cardName, 16, "#ffffff", FontWeight.BOLD), imagePlaceholder, stats);
        return cardSlot;
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
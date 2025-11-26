package views;

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

public class TradeView {

    private final Stage primaryStage;

    public TradeView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #383838;");

        VBox topBox = createTopControlArea(); // Title and Player Selection
        root.setTop(topBox);

        HBox tradeBox = createTradeSelectionArea(); // Side-by-side inventories
        root.setCenter(tradeBox);

        HBox buttonBox = createBottomButtonArea(); // Action Buttons
        root.setBottom(buttonBox);

        return new Scene(root, 1000, 700);
    }

    // Creates the title and opponent selection area at the top
    private VBox createTopControlArea() {
        VBox top = new VBox(15);
        top.setAlignment(Pos.CENTER);
        top.setStyle("-fx-background-color: #2a2a2a;");

        Label title = createLabel("Card Trade Request", 28, "#ffffff", FontWeight.BOLD);

        HBox playerSelect = new HBox(10);
        playerSelect.setAlignment(Pos.CENTER);
        Label lblPlayer = createLabel("Opponent Name:", 16, "#e0e0e0", FontWeight.NORMAL);
        TextField txtPlayer = new TextField();

        playerSelect.getChildren().addAll(lblPlayer, txtPlayer);
        top.getChildren().addAll(title, playerSelect);
        return top;
    }

    // Creates the main area with two lists for card selection
    private HBox createTradeSelectionArea() {
        HBox tradeBox = new HBox(30);
        tradeBox.setAlignment(Pos.CENTER);

        VBox playerOffer = createInventoryPanel("Your Offer (Select a card)", "#4CAF50");
        VBox opponentRequest = createInventoryPanel("Opponent's Card (Select a card)", "#F44336");

        tradeBox.getChildren().addAll(playerOffer, opponentRequest);
        return tradeBox;
    }

    // Creates a list panel for selecting cards from an inventory
    private VBox createInventoryPanel(String title, String color) {
        VBox panel = new VBox(10);
        panel.setPrefWidth(400);
        panel.setPrefHeight(450);
        panel.setStyle("-fx-background-color: #4a4a4a; -fx-border-color: " + color + "; -fx-border-width: 2;");

        ListView<String> cardListView = new ListView<>(); // List to display card names
        cardListView.getItems().addAll("My Dragon Card", "My Warrior", "My Shield", "Card to Trade");

        Label selectedCard = createLabel("Selected: None", 14, "#FFC107", FontWeight.NORMAL);

        panel.getChildren().addAll(createLabel(title, 18, "#ffffff", FontWeight.BOLD), selectedCard, cardListView);
        return panel;
    }

    // Creates the buttons at the bottom
    private HBox createBottomButtonArea() {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnSend = createActionButton("Send Trade Request", "#4CAF50");
        Button btnBack = createActionButton("Back to Menu", "#FFC107");

        btnBack.setOnAction(e -> new MenuView(primaryStage).show());

        buttonBox.getChildren().addAll(btnSend, btnBack);
        return buttonBox;
    }

    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Card Trade");
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
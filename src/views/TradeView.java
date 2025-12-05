package views;

import controller.MainController;
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
    private final MainController controller;

    public TradeView(Stage primaryStage, MainController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #383838;");

        VBox topBox = createTopControlArea();
        root.setTop(topBox);

        HBox tradeBox = createTradeSelectionArea();
        root.setCenter(tradeBox);

        HBox buttonBox = createBottomButtonArea();
        root.setBottom(buttonBox);

        return new Scene(root, 1000, 700);
    }

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

    private HBox createTradeSelectionArea() {
        HBox tradeBox = new HBox(30);
        tradeBox.setAlignment(Pos.CENTER);

        VBox playerOffer = createInventoryPanel("Your Offer (Select a card)", "#4CAF50");
        VBox opponentRequest = createInventoryPanel("Opponent's Card (Select a card)", "#F44336");

        tradeBox.getChildren().addAll(playerOffer, opponentRequest);
        return tradeBox;
    }

    private VBox createInventoryPanel(String title, String color) {
        VBox panel = new VBox(10);
        panel.setPrefWidth(400);
        panel.setPrefHeight(450);
        panel.setStyle("-fx-background-color: #4a4a4a; -fx-border-color: " + color + "; -fx-border-width: 2;");

        ListView<String> cardListView = new ListView<>();
        cardListView.getItems().addAll("My Dragon Card", "My Warrior", "My Shield", "Card to Trade");

        Label selectedCard = createLabel("Selected: None", 14, "#FFC107", FontWeight.NORMAL);

        panel.getChildren().addAll(createLabel(title, 18, "#ffffff", FontWeight.BOLD), selectedCard, cardListView);
        return panel;
    }

    private HBox createBottomButtonArea() {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnSend = createActionButton("Send Trade Request", "#7834CB", 250, 45);
        Button btnBack = createActionButton("Back to Menu", "#D9C6F0", 250, 45);

        btnBack.setOnAction(e -> controller.showMenu());

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

    private Button createActionButton(String text, String color, int width, int height) {
        Button btn = new Button(text);
        btn.setPrefSize(width, height);

        String styleNormal;
        String styleHover;

        if (color.equals("#7834CB")) {
            styleNormal = "-fx-background-color: #7834CB; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;";
            styleHover = "-fx-background-color: #9059D4; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;";
        } else if (color.equals("#D9C6F0")) {
            styleNormal = "-fx-background-color: #D9C6F0; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;";
            styleHover = "-fx-background-color: #F1EBFA; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;";
        } else {
            styleNormal = "-fx-background-color: " + color + "; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;";
            styleHover = styleNormal;
        }

        btn.setStyle(styleNormal);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(styleHover);
            btn.setCursor(javafx.scene.Cursor.HAND);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(styleNormal);
            btn.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        return btn;
    }
}
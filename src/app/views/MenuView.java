package app.views;

import app.controller.MainController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MenuView {

    private final Stage primaryStage;
    private final MainController controller;
    final String STYLE_NORMAL = "-fx-background-color: #A97DDE; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10;";
    final String STYLE_HOVER = "-fx-background-color: #9059D4; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10;";

    public MenuView(Stage primaryStage, MainController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    public Scene createScene() {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1e1e;");

        Label pseudoLabel = new Label("Utilisateur : " + controller.getLocalPlayer().getName());
        pseudoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pseudoLabel.setTextFill(Color.web("#A97DDE"));
        HBox topBar = new HBox(pseudoLabel);
        topBar.setAlignment(Pos.TOP_LEFT);
        root.setTop(topBar);

        VBox mainContent = new VBox(40);
        mainContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("ROBS CARD GAME");
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 60));
        titleLabel.setTextFill(Color.web("#f0f0f0"));

        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(450);

        Button combatButton = createButton("Launch Combat");
        Button inventoryButton = createButton("View Inventory");
        Button createCardButton = createButton("Create New Card");
        Button tradeButton = createButton("Request Card Trade");
        Button notificationButton = createButton("View Notifications");
        Button quitButton = createButton("Quit");

        for (Button b : new Button[]{combatButton, inventoryButton, createCardButton, tradeButton, notificationButton, quitButton}) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
        combatButton.setOnAction(e -> {
            controller.showCombat();
        });

        inventoryButton.setOnAction(e -> {
            controller.showInventory();
        });

        createCardButton.setOnAction(e -> {
            controller.showCardCreation();
        });

        tradeButton.setOnAction(e -> {
            controller.showTrade();
        });

        notificationButton.setOnAction(e -> controller.showNotifications());

        quitButton.setOnAction(e -> controller.quit());


        buttonContainer.getChildren().addAll(combatButton, inventoryButton, createCardButton, tradeButton, notificationButton, quitButton);
        mainContent.getChildren().addAll(titleLabel, buttonContainer);

        root.setCenter(mainContent);

        return new Scene(root, 800, 700);
    }

    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Main Menu");
        primaryStage.show();
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(60);
        btn.setStyle(STYLE_NORMAL);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(STYLE_HOVER);
            btn.setCursor(javafx.scene.Cursor.HAND);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(STYLE_NORMAL);
            btn.setCursor(javafx.scene.Cursor.DEFAULT);
        });
        return btn;
    }
}
package app.views;

import app.controller.MainController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

// Central navigation hub for accessing all features of the application
public class MenuView {

    private final Stage primaryStage;
    private final MainController controller;
    final String STYLE_NORMAL = "-fx-background-color: #A97DDE; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10;";
    final String STYLE_HOVER = "-fx-background-color: #9059D4; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10;";

    public MenuView(Stage primaryStage, MainController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    // Builds the menu scene containing action buttons for combat, inventory, etc.
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1e1e;");

        Label pseudoLabel = new Label("👤 " + controller.getLocalPlayer().getName());
        pseudoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pseudoLabel.setTextFill(Color.web("#A97DDE"));

        Button logoutBtn = new Button("Se déconnecter");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5555; " + "-fx-font-size: 12px; -fx-padding: 0; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> controller.logout());

        VBox userBox = new VBox(5, pseudoLabel, logoutBtn);
        userBox.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("R.O.B.S");
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 60));
        titleLabel.setTextFill(Color.web("#f0f0f0"));

        StackPane header = new StackPane(titleLabel, userBox);
        StackPane.setAlignment(userBox, Pos.TOP_LEFT);
        StackPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(header);

        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(600);

        Button combatButton = createButton("Lancer un Combat");
        Button inventoryButton = createButton("Voir son Inventaire");
        Button createCardButton = createButton("Créer une Carte");
        Button tradeButton = createButton("Échanger des Cartes");
        Button notificationButton = createButton("Notifications");
        Button quitButton = createButton("Quitter");

        // Sets uniform width for all primary action buttons
        for (Button b : new Button[]{combatButton, inventoryButton, createCardButton, tradeButton, notificationButton, quitButton}) {
            b.setMaxWidth(450);
        }

        combatButton.setOnAction(e -> controller.showCombat());
        inventoryButton.setOnAction(e -> controller.showInventory());
        createCardButton.setOnAction(e -> controller.showCardCreation());
        tradeButton.setOnAction(e -> controller.showTrade());
        notificationButton.setOnAction(e -> controller.showNotifications());
        quitButton.setOnAction(e -> controller.quit());

        buttonContainer.getChildren().addAll(titleLabel, combatButton, inventoryButton, createCardButton, tradeButton, notificationButton, quitButton);
        root.setCenter(buttonContainer);

        return new Scene(root, 800, 700);
    }

    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Main Menu");
        primaryStage.show();
    }

    // Helper method to create standard menu buttons with visual hover feedback
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
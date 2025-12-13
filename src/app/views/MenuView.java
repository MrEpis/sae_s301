package app.views;

import app.controller.MainController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MenuView {

    private final Stage primaryStage;
    private final MainController controller;
    final String STYLE_NORMAL = "-fx-background-color: #A97DDE; -fx-text-fill: black; -fx-font-weight: bold;";
    final String STYLE_HOVER = "-fx-background-color: #9059D4; -fx-text-fill: black; -fx-font-weight: bold;";

    public MenuView(Stage primaryStage, MainController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e1e1e;");

        Label titleLabel = new Label("ROBS CARD GAME");
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 40));
        titleLabel.setTextFill(Color.web("#f0f0f0"));

        Button combatButton = createButton("Launch Combat");
        Button inventoryButton = createButton("View Inventory");
        Button createCardButton = createButton("Create New Card");
        Button tradeButton = createButton("Request Card Trade");
        // NOUVEAU BOUTON
        Button notificationButton = createButton("View Notifications");
        Button quitButton = createButton("Quit");

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

        root.getChildren().addAll(
                titleLabel,
                combatButton,
                inventoryButton,
                createCardButton,
                tradeButton,
                notificationButton,
                quitButton
        );

        return new Scene(root, 600, 600);
    }

    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Main Menu");
        primaryStage.show();
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(300, 45);
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
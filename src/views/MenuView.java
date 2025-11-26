package views;

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

    public MenuView(Stage primaryStage) {
        this.primaryStage = primaryStage;
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
        Button quitButton = createButton("Quit");

        combatButton.setOnAction(e -> {
            new CombatView(primaryStage).show();
        });

        inventoryButton.setOnAction(e -> {
            new InventoryView(primaryStage).show();
        });

        createCardButton.setOnAction(e -> {
            new CardCreationView(primaryStage).show();
        });

        tradeButton.setOnAction(e -> {
            new TradeView(primaryStage).show();
        });

        quitButton.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(titleLabel, combatButton, inventoryButton, createCardButton, tradeButton, quitButton);

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
        btn.setStyle(
                "-fx-background-color: #555555; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold;"
        );
        return btn;
    }
}
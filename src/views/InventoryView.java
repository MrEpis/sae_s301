package views;

import controller.MainController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class InventoryView {

    private final Stage primaryStage;
    private final MainController controller;

    public InventoryView(Stage primaryStage, MainController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #222222;");

        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setStyle("-fx-background-color: #1a1a1a;");

        Button backButton = createActionButton("Back to Menu", "#D9C6F0", 180, 40);
        backButton.setOnAction(e -> controller.showMenu());

        topBox.getChildren().addAll(createLabel("Player Inventory", 28, "#ffffff", FontWeight.BOLD), backButton);
        root.setTop(topBox);

        VBox detailBox = createCardDetailPanel();
        detailBox.setId("DetailPanel");
        root.setRight(detailBox);

        ScrollPane scrollPane = createCardGridArea();
        root.setCenter(scrollPane);

        return new Scene(root, 1100, 750);
    }

    private ScrollPane createCardGridArea() {
        GridPane cardGrid = new GridPane();
        cardGrid.setHgap(20);
        cardGrid.setVgap(20);

        for (int i = 0; i < 50; i++) {
            VBox cardPlaceholder = createCardPlaceholder("Card " + (i + 1));
            cardPlaceholder.setOnMouseClicked(e -> updateDetails(primaryStage.getScene(), cardPlaceholder.getUserData().toString()));
            cardGrid.add(cardPlaceholder, i % 7, i / 7);
        }

        ScrollPane scrollPane = new ScrollPane(cardGrid);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private VBox createCardPlaceholder(String name) {
        VBox card = new VBox(5);
        card.setPrefSize(120, 160);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #333333; -fx-border-color: #aaaaaa; -fx-border-width: 2;");
        card.setUserData(name);

        card.getChildren().addAll(createLabel(name, 12, "#ffffff", FontWeight.BOLD), createLabel("Click for Info", 10, "#999999", FontWeight.NORMAL));
        return card;
    }

    private VBox createCardDetailPanel() {
        VBox detailBox = new VBox(15);
        detailBox.setPrefWidth(300);
        detailBox.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555555; -fx-border-width: 0 0 0 1;");
        detailBox.setAlignment(Pos.TOP_CENTER);

        Label selectedCardName = createLabel("No Card Selected", 18, "#ffffff", FontWeight.BOLD);

        VBox stats = new VBox(5);
        stats.getChildren().addAll(
                createLabel("HP: N/A", 14, "#cccccc", FontWeight.NORMAL),
                createLabel("ATK: N/A", 14, "#cccccc", FontWeight.NORMAL),
                createLabel("DEF: N/A", 14, "#cccccc", FontWeight.NORMAL)
        );
        stats.setUserData(new Label[]{(Label)stats.getChildren().get(0), (Label)stats.getChildren().get(1), (Label)stats.getChildren().get(2)});

        detailBox.getChildren().addAll(createLabel("Card Details", 20, "#FFC107", FontWeight.BOLD), selectedCardName, new Separator(), stats);
        detailBox.setUserData(new Object[]{selectedCardName, stats});

        return detailBox;
    }

    private void updateDetails(Scene scene, String cardName) {
        VBox detailBox = (VBox) scene.lookup("#DetailPanel");

        if (detailBox != null) {
            Label nameLabel = (Label) ((Object[])detailBox.getUserData())[0];
            VBox statsBox = (VBox) ((Object[])detailBox.getUserData())[1];

            nameLabel.setText(cardName);

            Label[] statLabels = (Label[]) statsBox.getUserData();
            statLabels[0].setText("HP: 50");
            statLabels[1].setText("ATK: 30");
            statLabels[2].setText("DEF: 20");
        }
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
            styleNormal = "-fx-background-color: #7834CB; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
            styleHover = "-fx-background-color: #9059D4; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
        } else if (color.equals("#D9C6F0")) {
            styleNormal = "-fx-background-color: #D9C6F0; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
            styleHover = "-fx-background-color: #F1EBFA; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
        } else {
            styleNormal = "-fx-background-color: " + color + "; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
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

    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Player Inventory");
        primaryStage.show();
    }
}
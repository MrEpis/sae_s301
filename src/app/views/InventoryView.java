package app.views;

import app.controller.InventoryController;
import app.model.Card;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;

public class InventoryView {

    private final Stage primaryStage;
    private final InventoryController controller;

    public InventoryView(Stage primaryStage, InventoryController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #222222;");

        Label pseudoLabel = createLabel("👤 " + controller.getLocalPlayer().getName(), 18, FontWeight.BOLD, "#A97DDE");

        VBox titleBox = new VBox(10);
        titleBox.setAlignment(Pos.CENTER);
        Label title = createLabel("Votre Inventaire", 28, FontWeight.BOLD, "#ffffff");

        Button backButton = createActionButton("Retour au Menu", "#D9C6F0", 180, 40);
        backButton.setOnAction(e -> controller.backToMenu());
        titleBox.getChildren().addAll(title, backButton);

        StackPane header = new StackPane(titleBox, pseudoLabel);
        StackPane.setAlignment(pseudoLabel, Pos.TOP_LEFT);
        root.setTop(header);

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
        cardGrid.setPadding(new javafx.geometry.Insets(20));

        int i = 0;
        for (Card card : controller.getPlayer().getInventory()) {
            VBox cardPlaceholder = createCardWidget(card);
            cardPlaceholder.setOnMouseClicked(e -> updateDetails(primaryStage.getScene(), card));
            cardGrid.add(cardPlaceholder, i % 5, i / 5);
            i++;
        }

        if (i == 0) {
            cardGrid.add(createLabel("Inventaire vide.", 16, FontWeight.NORMAL, "#999999"), 0, 0);
        }

        ScrollPane scrollPane = new ScrollPane(cardGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #222222; -fx-border-color: transparent;");
        return scrollPane;
    }

    private VBox createCardWidget(Card card) {
        VBox box = new VBox(5);
        box.setPrefSize(140, 220);
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("-fx-background-color: #333333; -fx-border-color: #7834CB; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

        ImageView imgView = new ImageView();
        imgView.setFitWidth(100);
        imgView.setFitHeight(100);
        imgView.setPreserveRatio(true);

        try {
            if (card.getImagePath() != null && !card.getImagePath().isEmpty()) {
                File file = new File(card.getImagePath());
                if (file.exists()) {
                    imgView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
        }

        box.getChildren().addAll(
                createLabel(card.getNom(), 14, FontWeight.BOLD, "#ffffff"),
                imgView,
                createLabel("PV: " + card.getHp(), 12, FontWeight.BOLD, "#4CAF50"),
                createLabel("ATK: " + card.getAtk(), 12, FontWeight.BOLD, "#F44336"),
                createLabel("DEF: " + card.getDef(), 12, FontWeight.BOLD, "#2196F3")
        );
        return box;
    }

    private VBox createCardDetailPanel() {
        VBox detailBox = new VBox(15);
        detailBox.setPrefWidth(300);
        detailBox.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555555; -fx-border-width: 0 0 0 1;");
        detailBox.setAlignment(Pos.TOP_CENTER);
        detailBox.setPadding(new javafx.geometry.Insets(20));

        Label selectedCardName = createLabel("Pas de carte sélectionnée", 18, FontWeight.BOLD, "#ffffff");

        ImageView detailImage = new ImageView();
        detailImage.setFitWidth(200);
        detailImage.setFitHeight(200);
        detailImage.setPreserveRatio(true);

        VBox stats = new VBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
                createLabel("PV: -", 16, FontWeight.NORMAL, "#4CAF50"),
                createLabel("ATK: -", 16, FontWeight.NORMAL, "#F44336"),
                createLabel("DEF: -", 16, FontWeight.NORMAL, "#2196F3")
        );

        detailBox.getChildren().addAll(createLabel("Détails de la carte", 22, FontWeight.BOLD, "#FFC107"), new Separator(), selectedCardName, detailImage, stats);

        detailBox.setUserData(new Object[]{selectedCardName, detailImage, stats});

        return detailBox;
    }

    private void updateDetails(Scene scene, Card card) {
        VBox detailBox = (VBox) scene.lookup("#DetailPanel");

        if (detailBox != null) {
            Object[] refs = (Object[]) detailBox.getUserData();
            Label nameLabel = (Label) refs[0];
            ImageView imgView = (ImageView) refs[1];
            VBox statsBox = (VBox) refs[2];

            nameLabel.setText(card.getNom());

            imgView.setImage(null);
            try {
                if (card.getImagePath() != null) {
                    File file = new File(card.getImagePath());
                    if (file.exists()) imgView.setImage(new Image(file.toURI().toString()));
                }
            } catch (Exception e) {
            }

            ((Label) statsBox.getChildren().get(0)).setText("PV: " + card.getHp());
            ((Label) statsBox.getChildren().get(1)).setText("ATK: " + card.getAtk());
            ((Label) statsBox.getChildren().get(2)).setText("DEF: " + card.getDef());
        }
    }

    private Label createLabel(String text, int size, FontWeight weight, String color) {
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
            styleNormal = "-fx-background-color: #7834CB; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;";
            styleHover = "-fx-background-color: #9059D4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;";
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
        primaryStage.setTitle("Inventaire");
        primaryStage.show();
    }
}
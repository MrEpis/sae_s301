package app.views;

import app.controller.MainController;
import app.controller.TradeController;
import app.model.Card;
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

public class TradeView {

    private final Stage primaryStage;
    private TradeController controller; // Référence au contrôleur

    private TextField opponentNameField;
    private Label statusLabel;
    private ListView<Card> playerCardList;
    private ListView<Card> opponentCardList;
    private Label offeredCardLabel;
    private Label requestedCardLabel;


    public TradeView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setController(TradeController controller) {
        this.controller = controller;
    }

    public void displayStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    // TODO: Ajouter une méthode pour remplir la liste de cartes de l'adversaire (ex: fillOpponentInventory(List<Card> cards))


    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
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
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10, 0, 20, 0));

        Label titleLabel = createLabel("Card Trade System", 28, "#ffffff", FontWeight.EXTRA_BOLD);
        statusLabel = createLabel("Enter opponent name and press Search.", 14, "#FFC107", FontWeight.NORMAL);

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);

        opponentNameField = new TextField();
        opponentNameField.setPromptText("Opponent Player Name");
        opponentNameField.setPrefWidth(200);

        Button searchButton = createActionButton("Search Opponent", "#C5CC8F", 180, 40);

        searchButton.setOnAction(e -> {
            if (controller != null && !opponentNameField.getText().trim().isEmpty()) {
                controller.searchOpponent(opponentNameField.getText().trim());
            }
        });

        searchBox.getChildren().addAll(opponentNameField, searchButton);
        topBox.getChildren().addAll(titleLabel, statusLabel, searchBox);
        return topBox;
    }

    private HBox createTradeSelectionArea() {
        HBox tradeBox = new HBox(20);
        tradeBox.setAlignment(Pos.TOP_CENTER);
        tradeBox.setPadding(new Insets(10));

        VBox playerPanel = createInventoryPanel("Your Inventory (Offered Card)", true);

        VBox summaryPanel = createSummaryPanel();

        VBox opponentPanel = createInventoryPanel("Opponent Inventory (Requested Card)", false);

        tradeBox.getChildren().addAll(playerPanel, summaryPanel, opponentPanel);
        return tradeBox;
    }

    private VBox createInventoryPanel(String title, boolean isLocalPlayer) {
        VBox panel = new VBox(10);
        panel.setPrefSize(350, 500);
        panel.setStyle("-fx-background-color: #4a4a4a; -fx-padding: 10; -fx-border-radius: 5;");

        ListView<Card> cardListView = new ListView<>();
        if (isLocalPlayer) {
            this.playerCardList = cardListView;
        } else {
            this.opponentCardList = cardListView;
        }

        // TODO: Ajouter une CellFactory pour afficher Card correctement (comme dans CombatView)
        // TODO: Ajouter un Listener pour mettre à jour les labels de carte sélectionnée

        Label selectedLabel = createLabel("Selected:", 14, "#D9C6F0", FontWeight.BOLD);
        Label cardDisplayLabel = createLabel("- None -", 16, "#ffffff", FontWeight.NORMAL);

        if (isLocalPlayer) {
            this.offeredCardLabel = cardDisplayLabel;
        } else {
            this.requestedCardLabel = cardDisplayLabel;
        }

        panel.getChildren().addAll(
                createLabel(title, 18, "#ffffff", FontWeight.BOLD),
                cardListView,
                selectedLabel,
                cardDisplayLabel
        );
        return panel;
    }

    private VBox createSummaryPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(200);

        Label arrow = createLabel("⇄", 48, "#D9C6F0", FontWeight.BOLD);

        VBox offeredBox = new VBox(5, createLabel("You Offer:", 16, "#ffffff", FontWeight.BOLD));
        offeredCardLabel = createLabel("Card A", 14, "#4CAF50", FontWeight.NORMAL); // Sera mis à jour
        offeredBox.getChildren().add(offeredCardLabel);
        offeredBox.setAlignment(Pos.CENTER);

        VBox requestedBox = new VBox(5, createLabel("You Request:", 16, "#ffffff", FontWeight.BOLD));
        requestedCardLabel = createLabel("Card B", 14, "#F44336", FontWeight.NORMAL); // Sera mis à jour
        requestedBox.getChildren().add(requestedCardLabel);
        requestedBox.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(arrow, offeredBox, requestedBox);
        return panel;
    }

    private HBox createBottomButtonArea() {
        HBox buttonBox = new HBox(40);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 10, 0));

        Button btnSend = createActionButton("Send Trade Request", "#7834CB", 250, 45);
        Button btnBack = createActionButton("Back to Menu", "#D9C6F0", 250, 45);

        btnBack.setOnAction(e -> {
            if (controller != null) {
                controller.backToMenu();
            }
        });

        btnSend.setOnAction(e -> {
            Card offered = playerCardList.getSelectionModel().getSelectedItem();
            Card requested = opponentCardList.getSelectionModel().getSelectedItem();
            String opponent = opponentNameField.getText().trim();

            if (offered != null && requested != null && !opponent.isEmpty() && controller != null) {
                controller.sendTradeRequest(opponent, offered.getNom(), requested.getNom());
            } else {
                displayStatus("Erreur: Veuillez sélectionner une carte dans chaque liste et rechercher un adversaire.");
            }
        });

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
            styleNormal = "-fx-background-color: #7834CB; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;";
            styleHover = "-fx-background-color: #9059D4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;";
        } else if (color.equals("#D9C6F0")) {
            styleNormal = "-fx-background-color: #D9C6F0; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
            styleHover = "-fx-background-color: #F1EBFA; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
        } else if (color.equals("#C5CC8F")) {
            styleNormal = "-fx-background-color: #C5CC8F; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
            styleHover = "-fx-background-color: #A97DDE; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;";
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
}
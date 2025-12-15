package app.views;

import app.controller.TradeController;
import app.model.Card;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority; // IMPORT AJOUTÉ POUR L'ALIGNEMENT
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class TradeView {

    // ... (Le début du fichier reste identique jusqu'à createInventoryPanel) ...
    private final Stage primaryStage;
    private TradeController controller;
    private ComboBox<String> opponentSelector;
    private Label statusLabel;
    private ListView<Card> playerCardList;
    private ListView<Card> opponentCardList;
    private Label offeredCardLabel;
    private Label requestedCardLabel;

    public TradeView(Stage primaryStage) { this.primaryStage = primaryStage; }
    public void setController(TradeController controller) { this.controller = controller; }
    public void displayStatus(String message) { if (statusLabel != null) statusLabel.setText(message); }
    public void updatePlayerList(List<String> players) {
        if (opponentSelector != null) {
            opponentSelector.getItems().clear(); opponentSelector.getItems().addAll(players);
            if (!players.isEmpty()) opponentSelector.getSelectionModel().selectFirst();
            else opponentSelector.setPromptText("Aucun joueur...");
        }
    }
    public void updateOpponentInventory(List<Card> cards) { if (opponentCardList != null) opponentCardList.getItems().setAll(cards); }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #383838;");
        VBox topBox = createTopControlArea(); root.setTop(topBox);
        HBox tradeBox = createTradeSelectionArea(); root.setCenter(tradeBox);
        HBox buttonBox = createBottomButtonArea(); root.setBottom(buttonBox);
        return new Scene(root, 1100, 750);
    }

    private VBox createTopControlArea() {
        VBox topBox = new VBox(15); topBox.setAlignment(Pos.CENTER); topBox.setPadding(new Insets(10, 0, 20, 0));
        Label titleLabel = createLabel("Card Trade System", 28, "#ffffff", FontWeight.EXTRA_BOLD);
        statusLabel = createLabel("Select an opponent to trade with.", 14, "#FFC107", FontWeight.NORMAL);
        HBox searchBox = new HBox(10); searchBox.setAlignment(Pos.CENTER);
        opponentSelector = new ComboBox<>(); opponentSelector.setPromptText("Select Player..."); opponentSelector.setPrefWidth(200);
        Button refreshButton = createActionButton("Refresh List", "#C5CC8F", 150, 40);
        refreshButton.setOnAction(e -> { if (controller != null) controller.refreshPlayerList(); });
        Button selectButton = createActionButton("Select Opponent", "#7834CB", 180, 40);
        selectButton.setOnAction(e -> {
            String selected = opponentSelector.getValue();
            if (controller != null && selected != null) controller.loadOpponentInventory(selected);
        });
        searchBox.getChildren().addAll(opponentSelector, refreshButton, selectButton);
        topBox.getChildren().addAll(titleLabel, statusLabel, searchBox);
        return topBox;
    }

    private HBox createTradeSelectionArea() {
        HBox tradeBox = new HBox(20); tradeBox.setAlignment(Pos.TOP_CENTER); tradeBox.setPadding(new Insets(10));
        VBox playerPanel = createInventoryPanel("Your Inventory (Offered Card)", true);
        VBox opponentPanel = createInventoryPanel("Opponent Inventory (Requested Card)", false);
        VBox summaryPanel = createSummaryPanel();
        tradeBox.getChildren().addAll(playerPanel, summaryPanel, opponentPanel);
        return tradeBox;
    }

    private VBox createInventoryPanel(String title, boolean isLocalPlayer) {
        VBox panel = new VBox(10);
        panel.setPrefSize(400, 550);
        panel.setStyle("-fx-background-color: #4a4a4a; -fx-padding: 10; -fx-border-radius: 5;");

        ListView<Card> cardListView = new ListView<>();
        cardListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Card item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null); setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(createCardListWidget(item));
                    setText(null);
                    // Style de sélection plus marqué
                    if (isSelected()) {
                        setStyle("-fx-background-color: #666666; -fx-border-color: #FFC107; -fx-border-width: 2;");
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
                    }
                }
            }
        });

        Label selectedLabel = createLabel("Selected:", 14, "#D9C6F0", FontWeight.BOLD);
        Label cardDisplayLabel = createLabel("- None -", 16, "#ffffff", FontWeight.NORMAL);

        if (isLocalPlayer) {
            this.playerCardList = cardListView; this.offeredCardLabel = cardDisplayLabel;
            if (controller != null) cardListView.getItems().setAll(controller.getLocalPlayerInventory());
        } else {
            this.opponentCardList = cardListView; this.requestedCardLabel = cardDisplayLabel;
        }

        cardListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) cardDisplayLabel.setText(newVal.getNom()); else cardDisplayLabel.setText("- None -");
        });

        panel.getChildren().addAll(createLabel(title, 18, "#ffffff", FontWeight.BOLD), cardListView, selectedLabel, cardDisplayLabel);
        return panel;
    }

    // --- MODIFICATION PRINCIPALE ICI ---
    private HBox createCardListWidget(Card card) {
        HBox cardBox = new HBox(15);
        cardBox.setAlignment(Pos.CENTER_LEFT);
        cardBox.setPadding(new Insets(5));

        // 1. Image
        ImageView imgView = new ImageView();
        imgView.setFitWidth(60); imgView.setFitHeight(60); imgView.setPreserveRatio(true);
        try { if (card.getImagePath() != null) { File file = new File(card.getImagePath()); if(file.exists()) imgView.setImage(new Image(file.toURI().toString())); } } catch(Exception e) {}

        // 2. Infos principales (Nom)
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        // CHANGEMENT DE COULEUR : #FFFFFF -> #FFC107 (Or) pour meilleure visibilité
        Label nameLbl = createLabel(card.getNom(), 16, "#FFC107", FontWeight.BOLD);
        infoBox.getChildren().add(nameLbl);

        // CORRECTION ALIGNEMENT : On dit à la boîte du nom de prendre toute la place disponible
        // Cela poussera la boîte des stats vers la droite.
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // 3. Stats (HP, ATK, DEF)
        VBox statsBox = new VBox(2);
        // On aligne le contenu à droite dans cette boîte
        statsBox.setAlignment(Pos.CENTER_RIGHT);
        // On fixe une largeur minimale pour que les stats soient bien alignées verticalement entre elles
        statsBox.setMinWidth(90);

        statsBox.getChildren().addAll(
                // Légère augmentation de la taille de police (12 -> 13)
                createLabel("HP: " + card.getHp(), 13, "#4CAF50", FontWeight.BOLD),
                createLabel("ATK: " + card.getAtk(), 13, "#F44336", FontWeight.BOLD),
                createLabel("DEF: " + card.getDef(), 13, "#2196F3", FontWeight.BOLD)
        );

        cardBox.getChildren().addAll(imgView, infoBox, statsBox);
        return cardBox;
    }
    // -----------------------------------

    // ... (Le reste du fichier est inchangé) ...
    private VBox createSummaryPanel() {
        VBox panel = new VBox(20); panel.setAlignment(Pos.CENTER); panel.setPrefWidth(200);
        Label arrow = createLabel("⇄", 48, "#D9C6F0", FontWeight.BOLD);
        VBox offeredBox = new VBox(5, createLabel("You Offer:", 16, "#ffffff", FontWeight.BOLD));
        Label centerOfferedLabel = createLabel("...", 14, "#4CAF50", FontWeight.NORMAL); offeredBox.getChildren().add(centerOfferedLabel); offeredBox.setAlignment(Pos.CENTER);
        VBox requestedBox = new VBox(5, createLabel("You Request:", 16, "#ffffff", FontWeight.BOLD));
        Label centerRequestedLabel = createLabel("...", 14, "#F44336", FontWeight.NORMAL); requestedBox.getChildren().add(centerRequestedLabel); requestedBox.setAlignment(Pos.CENTER);
        if (playerCardList != null) playerCardList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> centerOfferedLabel.setText(n != null ? n.getNom() : "..."));
        if (opponentCardList != null) opponentCardList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> centerRequestedLabel.setText(n != null ? n.getNom() : "..."));
        panel.getChildren().addAll(arrow, offeredBox, requestedBox);
        return panel;
    }

    private HBox createBottomButtonArea() {
        HBox buttonBox = new HBox(40); buttonBox.setAlignment(Pos.CENTER); buttonBox.setPadding(new Insets(20, 0, 10, 0));
        Button btnSend = createActionButton("Send Trade Request", "#7834CB", 250, 45);
        Button btnBack = createActionButton("Back to Menu", "#D9C6F0", 250, 45);
        btnBack.setOnAction(e -> { if (controller != null) controller.backToMenu(); });
        btnSend.setOnAction(e -> {
            Card offered = playerCardList.getSelectionModel().getSelectedItem();
            Card requested = opponentCardList.getSelectionModel().getSelectedItem();
            String opponent = opponentSelector.getValue();
            if (offered != null && requested != null && opponent != null && controller != null) controller.sendTradeRequest(opponent, offered.getId(), requested.getId());
            else displayStatus("Erreur: Veuillez sélectionner des cartes et un adversaire.");
        });
        buttonBox.getChildren().addAll(btnSend, btnBack);
        return buttonBox;
    }
    public void show() { primaryStage.setScene(createScene()); primaryStage.setTitle("Card Trade"); primaryStage.show(); }
    private Label createLabel(String text, int size, String color, FontWeight weight) {
        Label label = new Label(text); label.setFont(Font.font("Arial", weight, size)); label.setTextFill(Color.web(color)); return label;
    }
    private Button createActionButton(String text, String color, int width, int height) {
        Button btn = new Button(text); btn.setPrefSize(width, height); btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setOnMouseEntered(e -> btn.setCursor(javafx.scene.Cursor.HAND)); btn.setOnMouseExited(e -> btn.setCursor(javafx.scene.Cursor.DEFAULT)); return btn;
    }
}
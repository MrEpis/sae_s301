package app.views;

import app.controller.MainController;
import app.controller.TradeController;
import app.model.Card;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Interface used for selecting players and proposing card exchanges.
 * This view allows players to browse online opponents, view their inventories,
 * and select cards from both sides to form a trade agreement.
 */
public class TradeView {

    private final Stage primaryStage;
    private TradeController controller;
    private ComboBox<String> opponentSelector;
    private Label statusLabel;
    private ListView<Card> playerCardList;
    private ListView<Card> opponentCardList;
    private Label offeredCardLabel;
    private Label requestedCardLabel;

    /**
     * Initializes the TradeView with the primary stage.
     * @param primaryStage The main application window.
     */
    public TradeView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setController(TradeController controller) {
        this.controller = controller;
    }

    /**
     * Updates the text displayed in the footer status label.
     * @param message The feedback message for the user.
     */
    public void displayStatus(String message) {
        if (statusLabel != null) statusLabel.setText(message);
    }

    /**
     * Refreshes the dropdown list with the names of currently online players.
     * Automatically selects the first available player if the list is not empty.
     * @param players The list of usernames to display.
     */
    public void updatePlayerList(List<String> players) {
        if (opponentSelector != null) {
            opponentSelector.getItems().clear();
            opponentSelector.getItems().addAll(players);
            if (!players.isEmpty()) opponentSelector.getSelectionModel().selectFirst();
            else opponentSelector.setPromptText("Aucun joueur...");
        }
    }

    /**
     * Populates the opponent's card list with available cards from the selected target.
     * @param cards The collection of cards retrieved from the server.
     */
    public void updateOpponentInventory(List<Card> cards) {
        if (opponentCardList != null) opponentCardList.getItems().setAll(cards);
    }

    /**
     * Sets up the main trade screen layout.
     * Organizes the interface into a header, two side-by-side inventory panels
     * with a central summary, and a bottom action area.
     * @return The fully constructed Scene for trading.
     */
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #383838;");

        Label pseudoLabel = createLabel("👤 " + controller.getLocalPlayer().getName(), 18, "#FFC107", FontWeight.BOLD);
        VBox topBox = createTopControlArea();

        StackPane header = new StackPane(topBox, pseudoLabel);
        StackPane.setAlignment(pseudoLabel, Pos.TOP_LEFT);
        root.setTop(header);

        HBox tradeBox = createTradeSelectionArea();
        root.setCenter(tradeBox);

        HBox buttonBox = createBottomButtonArea();
        root.setBottom(buttonBox);

        return new Scene(root, 1100, 750);
    }

    /**
     * Builds the control section to select an opponent and refresh the player list.
     * @return A VBox containing the search controls.
     */
    private VBox createTopControlArea() {
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10, 0, 20, 0));


        Label titleLabel = createLabel("Échange de Cartes", 28, "#ffffff", FontWeight.EXTRA_BOLD);
        statusLabel = createLabel("Sélectionnez un joueur", 14, "#FFC107", FontWeight.NORMAL);
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        opponentSelector = new ComboBox<>();
        opponentSelector.setPromptText("Séléctionner un joueur...");
        opponentSelector.setPrefWidth(200);
        Button refreshButton = createActionButton("Rafraichir la liste", "#C5CC8F", 150, 40);
        refreshButton.setOnAction(e -> {
            if (controller != null) controller.refreshPlayerList();
        });
        Button selectButton = createActionButton("Choisir ce joueur", "#7834CB", 180, 40);
        selectButton.setOnAction(e -> {
            String selected = opponentSelector.getValue();
            if (controller != null && selected != null) controller.loadOpponentInventory(selected);
        });
        searchBox.getChildren().addAll(opponentSelector, refreshButton, selectButton);
        topBox.getChildren().addAll(titleLabel, statusLabel, searchBox);
        return topBox;
    }

    /**
     * Configures the central area showing both players' card lists and the trade summary.
     * @return An HBox containing the inventory and summary panels.
     */
    private HBox createTradeSelectionArea() {
        HBox tradeBox = new HBox(20);
        tradeBox.setAlignment(Pos.CENTER);
        tradeBox.setPadding(new Insets(10));

        VBox playerPanel = createInventoryPanel("Ton Inventaire", true);
        VBox opponentPanel = createInventoryPanel("Inventaire du receveur de l'offre", false);
        VBox summaryPanel = createSummaryPanel();

        HBox.setHgrow(playerPanel, Priority.ALWAYS);
        HBox.setHgrow(opponentPanel, Priority.ALWAYS);

        summaryPanel.setMinWidth(200);

        tradeBox.getChildren().addAll(playerPanel, summaryPanel, opponentPanel);
        return tradeBox;
    }

    /**
     * Creates an inventory panel with a customized ListCell factory for card rendering.
     * Manages selection events to update display labels.
     * @param title The header for the panel.
     * @param isLocalPlayer True if this panel belongs to the local user.
     * @return A VBox layout for the inventory panel.
     */
    private VBox createInventoryPanel(String title, boolean isLocalPlayer) {
        VBox panel = new VBox(10);
        panel.setMinWidth(300);
        panel.setMaxWidth(Double.MAX_VALUE);

        ListView<Card> cardListView = new ListView<>();
        cardListView.setStyle("-fx-background-color: #222222;");
        VBox.setVgrow(cardListView, Priority.ALWAYS);
        cardListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Card item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #222222;");
                } else {
                    setGraphic(createCardListWidget(item));
                    setText(null);
                    if (isSelected()) {
                        setStyle("-fx-background-color: #666666; -fx-border-color: #FFC107; -fx-border-width: 2;");
                    } else {
                        setStyle("-fx-background-color: #222222; -fx-border-width: 0;");
                    }
                }
            }
        });

        Label selectedLabel = createLabel("Selected:", 14, "#D9C6F0", FontWeight.BOLD);
        Label cardDisplayLabel = createLabel("pas de sélection", 16, "#ffffff", FontWeight.NORMAL);

        if (isLocalPlayer) {
            this.playerCardList = cardListView;
            this.offeredCardLabel = cardDisplayLabel;
            if (controller != null) cardListView.getItems().setAll(controller.getLocalPlayerInventory());
        } else {
            this.opponentCardList = cardListView;
            this.requestedCardLabel = cardDisplayLabel;
        }

        cardListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) cardDisplayLabel.setText(newVal.getNom());
            else cardDisplayLabel.setText("pas de sélection");
        });

        panel.getChildren().addAll(createLabel(title, 18, "#ffffff", FontWeight.BOLD), cardListView, selectedLabel, cardDisplayLabel);
        return panel;
    }

    /**
     * Generates a compact HBox row representing a single card.
     * Displays the thumbnail image along with health, attack, and defense stats.
     * @param card The Card data to render.
     * @return A styled HBox containing card info.
     */
    private HBox createCardListWidget(Card card) {
        HBox cardBox = new HBox(15);
        cardBox.setAlignment(Pos.CENTER_LEFT);
        cardBox.setPadding(new Insets(5));

        ImageView imgView = new ImageView();
        imgView.setFitWidth(60);
        imgView.setFitHeight(60);
        imgView.setPreserveRatio(true);
        try {
            if (card.getImagePath() != null) {
                File file = new File(card.getImagePath());
                if (file.exists()) imgView.setImage(new Image(file.toURI().toString()));
            }
        } catch (Exception e) {
        }

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = createLabel(card.getNom(), 16, "#FFC107", FontWeight.BOLD);
        infoBox.getChildren().add(nameLbl);

        HBox.setHgrow(infoBox, Priority.ALWAYS);

        VBox statsBox = new VBox(2);
        statsBox.setAlignment(Pos.CENTER_RIGHT);
        statsBox.setMinWidth(90);

        statsBox.getChildren().addAll(
                createLabel("PV: " + card.getHp(), 13, "#4CAF50", FontWeight.BOLD),
                createLabel("ATK: " + card.getAtk(), 13, "#F44336", FontWeight.BOLD),
                createLabel("DEF: " + card.getDef(), 13, "#2196F3", FontWeight.BOLD)
        );

        cardBox.getChildren().addAll(imgView, infoBox, statsBox);
        return cardBox;
    }

    /**
     * Displays the current trade offer summary in a central column.
     * Syncs with the list selections to show which cards are being exchanged.
     * @return A VBox summarizing the proposed deal.
     */
    private VBox createSummaryPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(200);
        Label arrow = createLabel("⇄", 48, "#D9C6F0", FontWeight.BOLD);
        VBox offeredBox = new VBox(5, createLabel("Ton offre:", 16, "#ffffff", FontWeight.BOLD));
        Label centerOfferedLabel = createLabel("...", 14, "#4CAF50", FontWeight.NORMAL);
        offeredBox.getChildren().add(centerOfferedLabel);
        offeredBox.setAlignment(Pos.CENTER);
        VBox requestedBox = new VBox(5, createLabel("Ta demande:", 16, "#ffffff", FontWeight.BOLD));
        Label centerRequestedLabel = createLabel("...", 14, "#F44336", FontWeight.NORMAL);
        requestedBox.getChildren().add(centerRequestedLabel);
        requestedBox.setAlignment(Pos.CENTER);
        if (playerCardList != null)
            playerCardList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> centerOfferedLabel.setText(n != null ? n.getNom() : "..."));
        if (opponentCardList != null)
            opponentCardList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> centerRequestedLabel.setText(n != null ? n.getNom() : "..."));
        panel.getChildren().addAll(arrow, offeredBox, requestedBox);
        return panel;
    }

    /**
     * Handles the user actions to send the trade request or return to the menu.
     * @return An HBox containing the primary action buttons.
     */
    private HBox createBottomButtonArea() {
        HBox buttonBox = new HBox(40);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 10, 0));
        Button btnSend = createActionButton("Envoyer la demande d'échange", "#7834CB", 250, 45);
        Button btnBack = createActionButton("Retour au Menu", "#D9C6F0", 250, 45);
        btnBack.setOnAction(e -> {
            if (controller != null) controller.backToMenu();
        });
        btnSend.setOnAction(e -> {
            Card offered = playerCardList.getSelectionModel().getSelectedItem();
            Card requested = opponentCardList.getSelectionModel().getSelectedItem();
            String opponent = opponentSelector.getValue();
            if (offered != null && requested != null && opponent != null && controller != null)
                controller.sendTradeRequest(opponent, offered.getId(), requested.getId());
            else displayStatus("Erreur: Veuillez sélectionner des cartes et un adversaire.");
        });
        buttonBox.getChildren().addAll(btnSend, btnBack);
        return buttonBox;
    }

    /**
     * Sets the scene to the primary stage and displays the window.
     */
    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Echange de Cartes");
        primaryStage.show();
    }

    /**
     * Utility method to create a styled Label.
     * @param text The text content.
     * @param size The font size.
     * @param color The web hex color code.
     * @param weight The font weight.
     * @return A configured Label.
     */
    private Label createLabel(String text, int size, String color, FontWeight weight) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", weight, size));
        label.setTextFill(Color.web(color));
        return label;
    }

    /**
     * Factory method for creating buttons with consistent style and hover feedback.
     * @param text The button text.
     * @param color The background color code.
     * @param width The preferred width.
     * @param height The preferred height.
     * @return A styled Button instance.
     */
    private Button createActionButton(String text, String color, int width, int height) {
        Button btn = new Button(text);
        btn.setPrefSize(width, height);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setOnMouseEntered(e -> btn.setCursor(javafx.scene.Cursor.HAND));
        btn.setOnMouseExited(e -> btn.setCursor(javafx.scene.Cursor.DEFAULT));
        return btn;
    }
}
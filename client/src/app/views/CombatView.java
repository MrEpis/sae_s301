package app.views;

import app.controller.CombatController;
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
 * Graphical interface for the combat arena.
 * Allows users to browse connected players, inspect their inventories,
 * and select cards to initiate a fight challenge.
 */
public class CombatView {

    private final Stage primaryStage;
    private final CombatController controller;

    private ComboBox<String> opponentSelector;
    private Label statusLabel;
    private ListView<Card> playerCardList;
    private ListView<Card> opponentCardList;

    private Label myFighterLabel;
    private Label targetLabel;

    /**
     * Initializes the CombatView with the primary stage and its controller.
     * @param primaryStage The main application window.
     * @param controller The controller managing combat logic.
     */
    public CombatView(Stage primaryStage, CombatController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
        this.controller.setView(this);
    }

    /**
     * Updates the status label message.
     * @param message The text to display.
     */
    public void displayStatus(String message) {
        if (statusLabel != null) statusLabel.setText(message);
    }

    /**
     * Updates the UI list of connected players.
     * @param playerNames The list of usernames to display.
     */
    public void updatePlayerList(List<String> players) {
        if (opponentSelector != null) {
            opponentSelector.getItems().clear();
            opponentSelector.getItems().addAll(players);
            if (!players.isEmpty()) opponentSelector.getSelectionModel().selectFirst();
        }
    }

    /**
     * Populates the opponent's card list with the retrieved inventory.
     * @param cards The list of cards belonging to the selected opponent.
     */
    public void updateOpponentInventory(List<Card> cards) {
        if (opponentCardList != null) {
            opponentCardList.getItems().setAll(cards);
        }
    }

    /**
     * Creates and organizes the visual components of the combat arena.
     * Sets up the header with the player's name, the selection area for cards,
     * and the bottom action buttons.
     * @return The fully constructed Scene for the combat view.
     */
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #383838;");

        Label pseudoLabel = createLabel("👤 " + controller.getLocalPlayer().getName(), 18, "#FF5252", FontWeight.BOLD);
        VBox topBox = createTopControlArea();

        StackPane header = new StackPane(topBox, pseudoLabel);
        StackPane.setAlignment(pseudoLabel, Pos.TOP_LEFT);
        root.setTop(header);

        HBox selectionArea = createSelectionArea();
        root.setCenter(selectionArea);

        HBox buttonBox = createBottomButtonArea();
        root.setBottom(buttonBox);

        controller.refreshPlayerList();

        return new Scene(root, 1100, 750);
    }

    /**
     * Builds the top section of the view containing opponent selection controls.
     * @return A VBox containing the title, status, and search controls.
     */
    private VBox createTopControlArea() {
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10, 0, 20, 0));

        Label titleLabel = createLabel("PRÉPARATION AU COMBAT", 32, "#FF5252", FontWeight.EXTRA_BOLD);
        statusLabel = createLabel("Choisissez votre adversaire.", 14, "#FFC107", FontWeight.NORMAL);

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);

        opponentSelector = new ComboBox<>();
        opponentSelector.setPromptText("Sélectionner un adversaire...");
        opponentSelector.setPrefWidth(200);

        Button refreshButton = createActionButton("Rafraichir la liste", "#fca503", 180, 40);
        refreshButton.setOnAction(e -> controller.refreshPlayerList());

        Button selectButton = createActionButton("Choisir Adversaire", "#D32F2F", 180, 40);
        selectButton.setOnAction(e -> {
            String selected = opponentSelector.getValue();
            if (selected != null) controller.loadOpponentInventory(selected);
        });

        searchBox.getChildren().addAll(opponentSelector, refreshButton, selectButton);
        topBox.getChildren().addAll(titleLabel, statusLabel, searchBox);
        return topBox;
    }

    /**
     * Organizes the central area showing both players' inventories and the VS summary.
     * @return An HBox containing the player panel, the summary, and the opponent panel.
     */
    private HBox createSelectionArea() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        VBox playerPanel = createCardListPanel("Ta Carte", true);
        VBox opponentPanel = createCardListPanel("Carte de l'Adversaire", false);
        VBox centerPanel = createSummaryPanel();

        HBox.setHgrow(playerPanel, Priority.ALWAYS);
        HBox.setHgrow(opponentPanel, Priority.ALWAYS);

        centerPanel.setMinWidth(200);

        box.getChildren().addAll(playerPanel, centerPanel, opponentPanel);
        return box;
    }

    /**
     * Builds a panel containing a scrollable list of cards with a custom cell factory.
     * @param title The title of the panel.
     * @param isLocalPlayer Whether the panel displays the local player's cards.
     * @return A VBox containing the card list and selection labels.
     */
    private VBox createCardListPanel(String title, boolean isLocalPlayer) {
        VBox panel = new VBox(10);
        panel.setMinWidth(300);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setStyle("-fx-background-color: #333; -fx-padding: 10; -fx-border-radius: 5; -fx-border-color: #D32F2F; -fx-border-width: 2;");

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
                        setStyle("-fx-background-color: #550000; -fx-border-color: #FF5252; -fx-border-width: 2;");
                    } else {
                        setStyle("-fx-background-color: #222222;");
                    }
                }
            }
        });

        Label selectedLabel = createLabel("Sélection:", 14, "#AAAAAA", FontWeight.BOLD);
        Label displayLabel = createLabel("- pas de sélection -", 16, "#ffffff", FontWeight.NORMAL);

        if (isLocalPlayer) {
            this.playerCardList = cardListView;
            this.myFighterLabel = displayLabel;
            cardListView.getItems().setAll(controller.getLocalPlayerInventory());
        } else {
            this.opponentCardList = cardListView;
            this.targetLabel = displayLabel;
        }

        cardListView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) displayLabel.setText(n.getNom());
            else displayLabel.setText("- None -");
        });

        panel.getChildren().addAll(createLabel(title, 20, "#FF5252", FontWeight.BOLD), cardListView, selectedLabel, displayLabel);
        return panel;
    }

    /**
     * Creates the visual widget (HBox) for a single card in a ListView.
     * Displays the image and the card's statistics (HP, ATK, DEF).
     * @param card The Card data to represent.
     * @return A styled HBox representing the card.
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
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        Label nameLbl = createLabel(card.getNom(), 16, "#FFC107", FontWeight.BOLD);
        infoBox.getChildren().add(nameLbl);

        VBox statsBox = new VBox(2);
        statsBox.setAlignment(Pos.CENTER_RIGHT);
        statsBox.setMinWidth(90);
        statsBox.getChildren().addAll(
                createLabel("PV: " + card.getHp(), 13, "#4CAF50", FontWeight.BOLD),
                createLabel("ATK: " + card.getAtk(), 13, "#FF5252", FontWeight.BOLD),
                createLabel("DEF: " + card.getDef(), 13, "#2196F3", FontWeight.BOLD)
        );

        cardBox.getChildren().addAll(imgView, infoBox, statsBox);
        return cardBox;
    }

    /**
     * Displays the currently selected cards from both sides in a central summary.
     * @return A VBox showing the names of selected fighters separated by a "VS" label.
     */
    private VBox createSummaryPanel() {
        VBox panel = new VBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(200);

        Label vs = createLabel("VS", 64, "#D32F2F", FontWeight.BLACK);

        VBox p1Box = new VBox(5, createLabel("Attaquant:", 14, "#AAA", FontWeight.NORMAL));
        p1Box.getChildren().add(myFighterLabel);
        p1Box.setAlignment(Pos.CENTER);

        VBox p2Box = new VBox(5, createLabel("Cible:", 14, "#AAA", FontWeight.NORMAL));
        p2Box.getChildren().add(targetLabel);
        p2Box.setAlignment(Pos.CENTER);

        if (playerCardList != null)
            playerCardList.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> myFighterLabel.setText(n != null ? n.getNom() : "..."));
        if (opponentCardList != null)
            opponentCardList.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> targetLabel.setText(n != null ? n.getNom() : "..."));

        panel.getChildren().addAll(p1Box, vs, p2Box);
        return panel;
    }

    /**
     * Sets up the bottom action area with buttons to initiate combat or return to the menu.
     * @return An HBox containing the "COMBATRE" and "Retraite" buttons.
     */
    private HBox createBottomButtonArea() {
        HBox buttonBox = new HBox(40);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 10, 0));

        Button btnFight = createActionButton("COMBATRE", "#D32F2F", 250, 50);

        Button btnBack = createActionButton("Retraite", "#757575", 150, 50);

        btnBack.setOnAction(e -> controller.backToMenu());

        btnFight.setOnAction(e -> {
            Card fighter = playerCardList.getSelectionModel().getSelectedItem();
            Card target = opponentCardList.getSelectionModel().getSelectedItem();
            String opponent = opponentSelector.getValue();

            if (fighter != null && target != null && opponent != null) {
                controller.sendFightRequest(opponent, fighter.getId(), target.getId());
            } else {
                displayStatus("Selectionnez un combattant, une cible et un adversaire !");
            }
        });

        buttonBox.getChildren().addAll(btnFight, btnBack);
        return buttonBox;
    }

    /**
     * Sets the scene to the primary stage and displays the window.
     */
    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Préparation au combat");
        primaryStage.show();
    }

    /**
     * Utility method to create a styled label.
     * @param text The text content.
     * @param size The font size.
     * @param color The web color code.
     * @param weight The font weight.
     * @return A configured Label instance.
     */
    private Label createLabel(String text, int size, String color, FontWeight weight) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", weight, size));
        label.setTextFill(Color.web(color));
        return label;
    }

    /**
     * Factory method for creating buttons with consistent styling and hover effects.
     * @param text The button text.
     * @param color The background color code.
     * @param width The preferred width.
     * @param height The preferred height.
     * @return A styled Button instance.
     */
    private Button createActionButton(String text, String color, int width, int height) {
        Button btn = new Button(text);
        btn.setPrefSize(width, height);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btn.setOnMouseEntered(e -> btn.setCursor(javafx.scene.Cursor.HAND));
        btn.setOnMouseExited(e -> btn.setCursor(javafx.scene.Cursor.DEFAULT));
        return btn;
    }
}
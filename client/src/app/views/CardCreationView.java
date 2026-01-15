package app.views;

import app.controller.CardCreationController;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.util.function.UnaryOperator;

/**
 * Represents the graphical user interface for creating new game cards.
 * This view allows users to name their cards, distribute a limited pool of stat points
 * (HP, ATK, DEF), and select a custom image with a real-time preview.
 */
public class CardCreationView {

    public static final int MAX_POINTS = 100;
    private final Stage primaryStage;
    private CardCreationController controller;
    private Label pointsLeftLabel;

    private ImageView cardImageView;
    private Label previewHpLabel;
    private Label previewAtkLabel;
    private Label previewDefLabel;
    private Spinner<Integer> hpSpinner;
    private Spinner<Integer> attackSpinner;
    private Spinner<Integer> defenseSpinner;
    private TextField cardNameField;
    private Label previewNameLabel;
    private Label errorLabel;

    /**
     * Constructs a new CardCreationView.
     * @param primaryStage The primary window stage for the application.
     */
    public CardCreationView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Updates the card preview with a selected image and name.
     * @param imagePath The URI or path of the chosen image.
     * @param cardName The default name derived from the file or existing input.
     */
    public void displayImagePreview(String imagePath, String cardName) {
        try {
            Image image = new Image(imagePath);
            cardImageView.setImage(image);
            cardImageView.setFitWidth(150);
            cardImageView.setFitHeight(150);
            cardImageView.setPreserveRatio(true);

            cardNameField.setText(cardName);
            previewNameLabel.setText(cardName);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
        }
    }

    public void displayError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    /**
     * Creates and organizes the main layout for the card creation scene.
     * @return The constructed Scene.
     */
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2e2e2e;");
        root.setPadding(new Insets(20));

        Label pseudoLabel = createLabel("👤 " + controller.getLocalPlayer().getName(), 18, "#A97DDE", FontWeight.BOLD);
        Label titleLabel = createTitleLabel("Créer une Carte");

        StackPane header = new StackPane(titleLabel, pseudoLabel);
        StackPane.setAlignment(pseudoLabel, Pos.TOP_LEFT);
        root.setTop(header);

        errorLabel = new Label("");
        errorLabel.setTextFill(Color.web("#FF5252"));
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox centerBox = new HBox(60);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        VBox formBox = createStatDistributionForm();
        VBox previewBox = createCardPreviewArea();

        HBox.setHgrow(formBox, Priority.ALWAYS);
        HBox.setHgrow(previewBox, Priority.ALWAYS);
        formBox.setMaxWidth(600);
        previewBox.setMaxWidth(600);

        centerBox.getChildren().addAll(leftSpacer, formBox, previewBox, rightSpacer);
        root.setCenter(centerBox);

        VBox footer = new VBox(10);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 0, 20, 0));

        HBox bottomBox = new HBox(40);
        bottomBox.setPadding(new Insets(30));
        bottomBox.setAlignment(Pos.CENTER);

        Button saveButton = createActionButton("Sauvegarder la carte", "#7834CB", 180, 40);
        Button backButton = createActionButton("Retour au Menu", "#D9C6F0", 180, 40);

        saveButton.setOnAction(e -> controller.saveCard(cardNameField.getText(), hpSpinner.getValue(), attackSpinner.getValue(), defenseSpinner.getValue()));
        backButton.setOnAction(e -> controller.backToMenu());

        bottomBox.getChildren().addAll(saveButton, backButton);
        footer.getChildren().addAll(errorLabel, bottomBox);
        root.setBottom(footer);

        return new Scene(root, 1100, 800);
    }

    /**
     * Builds the statistical input form including the name field and stat spinners.
     * @return A VBox containing the input form components.
     */
    private VBox createStatDistributionForm() {
        VBox form = new VBox(25);
        form.setPadding(new Insets(30));
        form.setMinWidth(400);
        form.setMaxWidth(Double.MAX_VALUE);
        form.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(form, Priority.ALWAYS);
        form.setStyle("-fx-background-color: #383838; -fx-border-color: #555555; -fx-border-width: 1;");

        pointsLeftLabel = createLabel("Points Remaining: 70", 20, "#FFC107", FontWeight.BOLD);
        GridPane statGrid = new GridPane();
        statGrid.setHgap(10);
        statGrid.setVgap(10);

        hpSpinner = new Spinner<>(1, MAX_POINTS, 10);
        attackSpinner = new Spinner<>(0, MAX_POINTS, 10);
        defenseSpinner = new Spinner<>(0, MAX_POINTS, 10);

        configureSpinnerBehavior(hpSpinner);
        configureSpinnerBehavior(attackSpinner);
        configureSpinnerBehavior(defenseSpinner);

        cardNameField = new TextField();
        cardNameField.setPromptText("Nom de la carte");

        statGrid.addRow(0, createLabel("Nom", 14, "#cccccc"), cardNameField);
        statGrid.addRow(1, createLabel("Points de vie (PV):", 14, "#cccccc"), hpSpinner);
        statGrid.addRow(2, createLabel("Attaque (ATK):", 14, "#cccccc"), attackSpinner);
        statGrid.addRow(3, createLabel("Défense (DEF):", 14, "#cccccc"), defenseSpinner);

        updateSpinnerLimits();

        addSafeListener(hpSpinner);
        addSafeListener(attackSpinner);
        addSafeListener(defenseSpinner);

        cardNameField.textProperty().addListener((obs, oldV, newV) -> {
            if (previewNameLabel != null) {
                previewNameLabel.setText(newV);
            }
        });

        cardNameField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if(newText.length() > 20 || !newText.matches("[0-9a-zA-ZàâäéèêëîïôöùûüÿçÀÂÄÉÈÊËÎÏÔÖÙÛÜŸÇ_-]*")) {
                return null;
            }
            return change;
        }));

        form.getChildren().addAll(createLabel("Statistique des Cartes", 16, "#ffffff"), pointsLeftLabel, statGrid);
        return form;
    }

    /**
     * Configures the behavior of a stat spinner, making it editable and applying numeric filters.
     * @param spinner The spinner to configure.
     */
    private void configureSpinnerBehavior(Spinner<Integer> spinner) {
        spinner.setEditable(true);

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                if (newText.isEmpty()) return change;
                try {
                    int val = Integer.parseInt(newText);
                    if (val <= MAX_POINTS) return change;
                } catch (NumberFormatException e) {
                }
            }
            return null;
        };

        TextFormatter<Integer> textFormatter = new TextFormatter<>(new IntegerStringConverter(), spinner.getValue(), filter);
        spinner.getEditor().setTextFormatter(textFormatter);

        spinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                try {
                    spinner.increment(0);
                } catch (Exception e) {
                    spinner.getEditor().setText(String.valueOf(spinner.getValue()));
                }
            }
        });
    }

    /**
     * Adds a listener to a spinner to ensure the total points across all stats do not exceed MAX_POINTS.
     * @param spinner The spinner to monitor.
     */
    private void addSafeListener(Spinner<Integer> spinner) {
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            int total = hpSpinner.getValue() + attackSpinner.getValue() + defenseSpinner.getValue();

            if (total > MAX_POINTS) {
                spinner.getValueFactory().setValue(oldVal);
            } else {
                updateSpinnerLimits();
            }
        });
    }

    /**
     * Updates the maximum allowed values for each spinner based on points already spent.
     */
    private void updateSpinnerLimits() {
        int currentHp = hpSpinner.getValue();
        int currentAtk = attackSpinner.getValue();
        int currentDef = defenseSpinner.getValue();

        int maxForHp = MAX_POINTS - (currentAtk + currentDef);
        int maxForAtk = MAX_POINTS - (currentHp + currentDef);
        int maxForDef = MAX_POINTS - (currentHp + currentAtk);

        ((SpinnerValueFactory.IntegerSpinnerValueFactory) hpSpinner.getValueFactory()).setMax(maxForHp);
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) attackSpinner.getValueFactory()).setMax(maxForAtk);
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) defenseSpinner.getValueFactory()).setMax(maxForDef);

        int pointsLeft = MAX_POINTS - (currentHp + currentAtk + currentDef);
        pointsLeftLabel.setText("Points Restants: " + pointsLeft);

        updateCardPreview();
    }

    /**
     * Syncs the statistical values from the spinners to the card preview labels.
     */
    private void updateCardPreview() {
        if (previewHpLabel != null && hpSpinner != null) {
            previewHpLabel.setText("HP: " + hpSpinner.getValue());
            previewAtkLabel.setText("ATK: " + attackSpinner.getValue());
            previewDefLabel.setText("DEF: " + defenseSpinner.getValue());
        }
    }

    /**
     * Creates the right-side area showing the real-time card preview and image selection button.
     * @return A VBox containing the preview components.
     */
    private VBox createCardPreviewArea() {
        VBox preview = new VBox(10);
        preview.setPrefSize(300, 400);
        preview.setAlignment(Pos.TOP_CENTER);

        VBox cardTemplate = createCardTemplate();

        Button selectImageButton = createActionButton("Séléctionner une image", "#C5CC8F", 200, 40);
        selectImageButton.setOnAction(e -> controller.chooseImageFile());

        preview.getChildren().addAll(cardTemplate, selectImageButton);
        return preview;
    }

    /**
     * Constructs the visual template for the card preview (the actual "card" look).
     * @return A VBox styled as a game card.
     */
    private VBox createCardTemplate() {
        VBox cardSlot = new VBox(10);
        cardSlot.setPadding(new Insets(20));
        cardSlot.setPrefSize(300, 500);
        cardSlot.setAlignment(Pos.TOP_CENTER);

        cardSlot.setStyle(
                "-fx-background-color: #383838; " +
                        "-fx-border-color: #D9C6F0; " +
                        "-fx-border-width: 4; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );

        previewNameLabel = createLabel("NOM DE CARTE", 22, "#ffffff", FontWeight.BOLD);
        previewNameLabel.setPadding(new Insets(2, 5, 0, 0));

        HBox nameBar = new HBox();
        nameBar.setAlignment(Pos.TOP_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        nameBar.getChildren().addAll(spacer, previewNameLabel);

        cardImageView = new ImageView();
        cardImageView.setFitWidth(250);
        cardImageView.setFitHeight(250);

        VBox imageContainer = new VBox(cardImageView);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefSize(160, 160);
        imageContainer.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #7834CB; " +
                        "-fx-border-width: 2; " +
                        "-fx-padding: 5;" +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );
        VBox.setMargin(imageContainer, new Insets(0, 0, 5, 0));

        VBox statsBox = new VBox(15);
        previewHpLabel = createLabel("HP: 10", 28, "#4CAF50", FontWeight.BOLD);
        previewAtkLabel = createLabel("ATK: 10", 28, "#F44336", FontWeight.BOLD);
        previewDefLabel = createLabel("DEF: 10", 28, "#2196F3", FontWeight.BOLD);

        statsBox.getChildren().addAll(previewHpLabel, previewAtkLabel, previewDefLabel);
        cardSlot.getChildren().addAll(previewNameLabel, imageContainer, statsBox);

        return cardSlot;
    }

    /**
     * Utility to create the main title label for the view.
     * @param text The text for the title.
     * @return A styled Label.
     */
    private Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 48));
        label.setTextFill(Color.web("#ffffff"));
        return label;
    }

    /**
     * Utility to create a basic label with specific font size and color.
     * @param text Label text.
     * @param size Font size.
     * @param color Web color code.
     * @return A styled Label.
     */
    private Label createLabel(String text, int size, String color) {
        return createLabel(text, size, color, FontWeight.NORMAL);
    }

    /**
     * Overloaded utility to create a label with specific font weight.
     * @param text Label text.
     * @param size Font size.
     * @param color Web color code.
     * @param weight Font weight.
     * @return A styled Label.
     */
    private Label createLabel(String text, int size, String color, FontWeight weight) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", weight, size));
        label.setTextFill(Color.web(color));
        return label;
    }

    /**
     * Factory method for creating buttons with consistent hover effects and styles.
     * @param text Button text.
     * @param color Primary color.
     * @param width Preferred width.
     * @param height Preferred height.
     * @return A styled Button.
     */
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

    /**
     * Initializes the stage with the creation scene and shows it.
     */
    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Card Creation");

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public void setController(CardCreationController controller) {
        this.controller = controller;
    }

    public String getCardNameInput() {
        return cardNameField.getText();
    }

    public Scene getScene() {
        return cardNameField.getScene();
    }
}
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


    public CardCreationView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setController(CardCreationController controller) {
        this.controller = controller;
    }

    public String getCardNameInput() {
        return cardNameField.getText();
    }

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

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2e2e2e;");

        Label titleLabel = createTitleLabel("Create Your Card");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        HBox centerBox = new HBox(30);
        centerBox.setAlignment(Pos.CENTER);

        VBox formBox = createStatDistributionForm();
        VBox previewBox = createCardPreviewArea();

        centerBox.getChildren().addAll(formBox, previewBox);
        root.setCenter(centerBox);

        HBox bottomBox = new HBox(20);
        bottomBox.setAlignment(Pos.CENTER);

        Button saveButton = createActionButton("Save Card", "#7834CB", 180, 40);
        Button backButton = createActionButton("Back to Menu", "#D9C6F0", 180, 40);

        saveButton.setOnAction(e -> {
            controller.saveCard(
                    cardNameField.getText(),
                    hpSpinner.getValue(),
                    attackSpinner.getValue(),
                    defenseSpinner.getValue()
            );
        });

        backButton.setOnAction(e -> controller.backToMenu());

        bottomBox.getChildren().addAll(saveButton, backButton);
        root.setBottom(bottomBox);

        return new Scene(root, 800, 600);
    }

    private VBox createStatDistributionForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        form.setPrefWidth(350);
        form.setStyle("-fx-background-color: #383838; -fx-border-color: #555555; -fx-border-width: 1;");

        pointsLeftLabel = createLabel("Points Remaining: " + (MAX_POINTS - 30), 14, "#FFC107");

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

        statGrid.addRow(0, createLabel("Name:", 14, "#cccccc"), cardNameField);
        statGrid.addRow(1, createLabel("Health (HP):", 14, "#cccccc"), hpSpinner);
        statGrid.addRow(2, createLabel("Attack (ATK):", 14, "#cccccc"), attackSpinner);
        statGrid.addRow(3, createLabel("Defense (DEF):", 14, "#cccccc"), defenseSpinner);

        updateSpinnerLimits();

        addSafeListener(hpSpinner);
        addSafeListener(attackSpinner);
        addSafeListener(defenseSpinner);

        cardNameField.textProperty().addListener((obs, oldV, newV) -> {
            if (previewNameLabel != null) {
                previewNameLabel.setText(newV);
            }
        });

        form.getChildren().addAll(createLabel("Card Properties", 16, "#ffffff"), pointsLeftLabel, statGrid);
        return form;
    }

    private void configureSpinnerBehavior(Spinner<Integer> spinner) {
        spinner.setEditable(true);

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                if (newText.isEmpty()) return change;
                try {
                    int val = Integer.parseInt(newText);
                    if (val <= MAX_POINTS) return change;
                } catch (NumberFormatException e) { }
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
        pointsLeftLabel.setText("Points Remaining: " + pointsLeft);

        updateCardPreview();
    }

    private void updateCardPreview() {
        if (previewHpLabel != null && hpSpinner != null) {
            previewHpLabel.setText("HP: " + hpSpinner.getValue());
            previewAtkLabel.setText("ATK: " + attackSpinner.getValue());
            previewDefLabel.setText("DEF: " + defenseSpinner.getValue());
        }
    }

    private VBox createCardPreviewArea() {
        VBox preview = new VBox(10);
        preview.setPrefSize(300, 400);
        preview.setAlignment(Pos.TOP_CENTER);

        VBox cardTemplate = createCardTemplate();

        Button selectImageButton = createActionButton("Select Image File", "#C5CC8F", 180, 40);
        selectImageButton.setOnAction(e -> controller.chooseImageFile());

        preview.getChildren().addAll(cardTemplate, selectImageButton);
        return preview;
    }

    private VBox createCardTemplate() {
        VBox cardSlot = new VBox(5);
        cardSlot.setPadding(new Insets(5));
        cardSlot.setPrefSize(170, 300);
        cardSlot.setAlignment(Pos.TOP_CENTER);

        cardSlot.setStyle(
                "-fx-background-color: #383838; " +
                        "-fx-border-color: #D9C6F0; " +
                        "-fx-border-width: 4; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );

        previewNameLabel = createLabel("NOM DE CARTE", 12, "#ffffff");
        previewNameLabel.setPadding(new Insets(2, 5, 0, 0));

        HBox nameBar = new HBox();
        nameBar.setAlignment(Pos.TOP_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        nameBar.getChildren().addAll(spacer, previewNameLabel);

        cardImageView = new ImageView();
        cardImageView.setFitWidth(150);
        cardImageView.setFitHeight(150);

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

        VBox statsBox = new VBox(3);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(0, 0, 0, 5));

        previewHpLabel = createLabel("HP: 10", 18, "#4CAF50", FontWeight.BOLD);
        previewAtkLabel = createLabel("ATK: 10", 18, "#F44336", FontWeight.BOLD);
        previewDefLabel = createLabel("DEF: 10", 18, "#2196F3", FontWeight.BOLD);

        statsBox.getChildren().addAll(previewHpLabel, previewAtkLabel, previewDefLabel);

        cardSlot.getChildren().addAll(nameBar, imageContainer, statsBox);

        return cardSlot;
    }

    public Scene getScene() {
        return cardNameField.getScene();
    }

    private Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 32));
        label.setTextFill(Color.web("#ffffff"));
        return label;
    }

    private Label createLabel(String text, int size, String color) {
        return createLabel(text, size, color, FontWeight.NORMAL);
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

    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Card Creation");
        primaryStage.show();
    }
}
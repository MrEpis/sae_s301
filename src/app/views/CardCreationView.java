package app.views;

import app.controller.CardCreationController;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

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
    private TextField previewNameField;


    public CardCreationView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setController(CardCreationController controller) {
        this.controller = controller;
    }

    public void displayImagePreview(String imagePath, String cardName) {
        try {
            Image image = new Image(imagePath);
            cardImageView.setImage(image);

            cardImageView.setFitWidth(150);
            cardImageView.setFitHeight(150);
            cardImageView.setPreserveRatio(true);

            cardNameField.setText(cardName);
            previewNameField.setText(cardName);

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

        hpSpinner = createStatSpinner(0, MAX_POINTS, 10);
        attackSpinner = createStatSpinner(0, MAX_POINTS, 10);
        defenseSpinner = createStatSpinner(0, MAX_POINTS, 10);

        cardNameField = new TextField("Nom de la Carte");

        statGrid.addRow(0, createLabel("Name:", 14, "#cccccc"), cardNameField);
        statGrid.addRow(1, createLabel("Health (HP):", 14, "#cccccc"), hpSpinner);
        statGrid.addRow(2, createLabel("Attack (ATK):", 14, "#cccccc"), attackSpinner);
        statGrid.addRow(3, createLabel("Defense (DEF):", 14, "#cccccc"), defenseSpinner);

        setupSpinnerListeners(hpSpinner, attackSpinner, defenseSpinner);

        cardNameField.textProperty().addListener((obs, oldV, newV) -> {
            previewNameField.setText(newV);
        });

        form.getChildren().addAll(createLabel("Card Properties", 16, "#ffffff"), pointsLeftLabel, statGrid);
        return form;
    }

    private VBox createCardPreviewArea() {
        VBox preview = new VBox(10);
        preview.setPrefSize(300, 400);
        preview.setAlignment(Pos.TOP_CENTER);

        previewNameField = new TextField("NOM DE CARTE");
        previewNameField.setEditable(false);
        previewNameField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");

        VBox cardTemplate = createCardTemplate();

        Button selectImageButton = createActionButton("Select Image File", "#C5CC8F", 180, 40);
        selectImageButton.setOnAction(e -> controller.chooseImageFile());

        preview.getChildren().addAll(previewNameField, cardTemplate, selectImageButton);
        return preview;
    }

    private VBox createCardTemplate() {
        VBox cardSlot = new VBox(5);
        cardSlot.setPadding(new Insets(5));
        cardSlot.setPrefSize(200, 300);
        cardSlot.setAlignment(Pos.TOP_CENTER);

        cardSlot.setStyle(
                "-fx-background-color: #EBD7D3; " +
                        "-fx-border-color: #D9C6F0; " +
                        "-fx-border-width: 4; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );

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
        VBox.setMargin(imageContainer, new Insets(5, 0, 5, 0));

        VBox statsBox = new VBox(3);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(0, 0, 0, 5));

        previewHpLabel = createLabel("HP: 10", 24, "#4CAF50", FontWeight.BOLD);
        previewAtkLabel = createLabel("ATK: 10", 24, "#F44336", FontWeight.BOLD);
        previewDefLabel = createLabel("DEF: 10", 24, "#2196F3", FontWeight.BOLD);

        statsBox.getChildren().addAll(previewHpLabel, previewAtkLabel, previewDefLabel);

        cardSlot.getChildren().addAll(imageContainer, statsBox);

        return cardSlot;
    }

    private void updateCardPreview() {
        if (previewHpLabel != null && hpSpinner != null) {
            previewHpLabel.setText("HP: " + hpSpinner.getValue());
            previewAtkLabel.setText("ATK: " + attackSpinner.getValue());
            previewDefLabel.setText("DEF: " + defenseSpinner.getValue());
        }
    }


    private Spinner<Integer> createStatSpinner(int min, int max, int initial) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial);
        spinner.setEditable(true);
        spinner.setPrefWidth(100);
        return spinner;
    }

    private void setupSpinnerListeners(Spinner<Integer> hp, Spinner<Integer> atk, Spinner<Integer> def) {
        ChangeListener<Integer> listener = (obs, oldValue, newValue) -> {
            int currentTotal = hp.getValue() + atk.getValue() + def.getValue();
            int pointsLeft = MAX_POINTS - currentTotal;

            if (currentTotal > MAX_POINTS) {
                ((Spinner<Integer>) obs).getValueFactory().setValue(oldValue);
                pointsLeft = MAX_POINTS - (hp.getValue() + atk.getValue() + def.getValue());
            }

            pointsLeftLabel.setText("Points Remaining: " + pointsLeft);
            updateCardPreview();
        };

        hp.valueProperty().addListener(listener);
        atk.valueProperty().addListener(listener);
        def.valueProperty().addListener(listener);
    }

    public File openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner l'image de la carte");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Fichiers images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser.showOpenDialog(primaryStage);
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
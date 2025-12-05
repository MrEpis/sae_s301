package views;

import controller.MainController;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class CardCreationView {

    public static final int MAX_POINTS = 100;
    private final Stage primaryStage;
    private final MainController controller;
    private Label pointsLeftLabel;

    public CardCreationView(Stage primaryStage, MainController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2e2e2e;");

        Label titleLabel = createTitleLabel("Create Your Card");
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
            // Logique de sauvegarde ici
        });
        backButton.setOnAction(e -> controller.showMenu());

        bottomBox.getChildren().addAll(saveButton, backButton);
        root.setBottom(bottomBox);

        return new Scene(root, 800, 600);
    }

    private VBox createStatDistributionForm() {
        VBox form = new VBox(15);
        form.setPrefWidth(350);
        form.setStyle("-fx-background-color: #383838; -fx-border-color: #555555; -fx-border-width: 1;");

        pointsLeftLabel = createLabel("Points Remaining: " + MAX_POINTS, 14, "#FFC107");

        GridPane statGrid = new GridPane();
        statGrid.setHgap(10);
        statGrid.setVgap(10);

        Spinner<Integer> hpSpinner = createStatSpinner(0, MAX_POINTS, 10);
        Spinner<Integer> attackSpinner = createStatSpinner(0, MAX_POINTS, 10);
        Spinner<Integer> defenseSpinner = createStatSpinner(0, MAX_POINTS, 10);

        statGrid.addRow(0, createLabel("Health (HP):", 14, "#cccccc"), hpSpinner);
        statGrid.addRow(1, createLabel("Attack (ATK):", 14, "#cccccc"), attackSpinner);
        statGrid.addRow(2, createLabel("Defense (DEF):", 14, "#cccccc"), defenseSpinner);

        setupSpinnerListeners(hpSpinner, attackSpinner, defenseSpinner);

        form.getChildren().addAll(createLabel("Allocate Stat Points", 16, "#ffffff"), pointsLeftLabel, statGrid);
        return form;
    }

    private VBox createCardPreviewArea() {
        VBox preview = new VBox(15);
        preview.setPrefSize(300, 350);
        preview.setAlignment(Pos.TOP_CENTER);
        preview.setStyle("-fx-background-color: #4a4a4a; -fx-border-color: #cccccc; -fx-border-width: 2;");

        TextField cardNameField = new TextField("My Custom Card");

        Button selectImageButton = createActionButton("Select Image File", "#C5CC8F", 180, 40);
        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setPrefSize(180, 180);

        preview.getChildren().addAll(createLabel("Card Name:", 14, "#ffffff"), cardNameField, createLabel("Image:", 14, "#ffffff"), selectImageButton, imagePlaceholder);
        return preview;
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
            pointsLeftLabel.setText("Points Remaining: " + pointsLeft);

            if (currentTotal > MAX_POINTS) {
                ((Spinner<Integer>) obs).getValueFactory().setValue(oldValue);
                pointsLeftLabel.setText("Points Remaining: " + (MAX_POINTS - (hp.getValue() + atk.getValue() + def.getValue())));
            }
        };

        hp.valueProperty().addListener(listener);
        atk.valueProperty().addListener(listener);
        def.valueProperty().addListener(listener);
    }

    private Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 32));
        label.setTextFill(Color.web("#ffffff"));
        return label;
    }

    private Label createLabel(String text, int size, String color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", size));
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
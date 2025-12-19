package app.views;

import app.controller.MainController;
import app.model.Card;
import app.model.FightResultModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;

public class FightResultView {

    private final Stage stage;
    private final MainController controller;
    private final FightResultModel result;
    private final Card myCard;

    public FightResultView(Stage stage, MainController controller, FightResultModel result, Card myCard) {
        this.stage = stage;
        this.controller = controller;
        this.result = result;
        this.myCard = myCard;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #D32F2F; -fx-border-width: 5;");

        // Header : Pseudo à gauche, Titre au centre
        Label pseudoLabel = new Label("👤 " + controller.getLocalPlayer().getName());
        pseudoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pseudoLabel.setTextFill(Color.web("#A97DDE"));

        Label title = new Label("RAPPORT DE COMBAT");
        title.setTextFill(Color.web("#FF5252"));
        title.setFont(Font.font("Arial", FontWeight.BLACK, 32));

        StackPane header = new StackPane(title, pseudoLabel);
        StackPane.setAlignment(pseudoLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(title, Pos.CENTER);
        root.setTop(header);

        // Conteneur principal au centre
        VBox centerBox = new VBox(50);
        centerBox.setAlignment(Pos.CENTER);

        // Message de log (ex: L'adversaire a fui le combat)
        Label logLabel = new Label(result.getLogMessage());
        logLabel.setTextFill(Color.web("#FFC107"));
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24)); // Légèrement plus gros pour l'impact
        logLabel.setWrapText(true);
        logLabel.setTextAlignment(TextAlignment.CENTER);
        logLabel.setAlignment(Pos.CENTER); // Correction : Aligne le texte au centre du Label
        logLabel.setMaxWidth(750);
        logLabel.setPrefWidth(750); // Assure que le label prend de la place pour le centrage horizontal

        centerBox.getChildren().add(logLabel);

        // N'affiche l'arène que s'il y a un adversaire (pas de fuite)
        if (result.getOpponentCard() != null) {
            HBox arena = new HBox(60);
            arena.setAlignment(Pos.CENTER);

            VBox mySide = (myCard != null)
                    ? createFighterCard(myCard, "Moi", myCard.getHp() <= 0, Color.GREEN)
                    : new VBox(new Label("?"));

            Label vs = new Label("VS");
            vs.setTextFill(Color.GRAY);
            vs.setFont(Font.font("Arial", FontWeight.BLACK, 40));

            VBox oppSide = createFighterCard(result.getOpponentCard(), "Adversaire", result.getOpponentCard().getHp() <= 0, Color.RED);

            arena.getChildren().addAll(mySide, vs, oppSide);
            centerBox.getChildren().add(arena);
        }

        // Bouton de fermeture
        Button closeBtn = new Button("Fermer le rapport");
        closeBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 5;");
        closeBtn.setPrefSize(250, 50);
        closeBtn.setOnAction(e -> controller.showNotifications());

        centerBox.getChildren().add(closeBtn);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 900, 650);
        stage.setScene(scene);
        stage.sizeToScene(); // Important pour Linux
        stage.show();
    }

    private VBox createFighterCard(Card card, String ownerTitle, boolean isDead, Color themeColor) {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);

        Label lblOwner = new Label(ownerTitle);
        lblOwner.setTextFill(themeColor);
        lblOwner.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        VBox cardBox = new VBox(5);
        cardBox.setPrefSize(180, 260);
        cardBox.setAlignment(Pos.TOP_CENTER);
        String borderColor = isDead ? "#555555" : themeColor.toString().replace("0x", "#");
        cardBox.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: " + borderColor + "; -fx-border-width: 3; -fx-border-radius: 8;");

        Label name = new Label(card.getNom());
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        ImageView img = new ImageView();
        img.setFitWidth(100);
        img.setFitHeight(100);
        img.setPreserveRatio(true);
        try {
            if (card.getImagePath() != null) {
                File f = new File(card.getImagePath());
                if (f.exists()) img.setImage(new Image(f.toURI().toString()));
            }
        } catch (Exception e) {}

        Label hpLabel = new Label(isDead ? "MORT" : "PV: " + card.getHp());
        hpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        hpLabel.setTextFill(isDead ? Color.GRAY : Color.web("#4CAF50"));

        Label stats = new Label("ATK: " + card.getAtk() + " | DEF: " + card.getDef());
        stats.setTextFill(Color.LIGHTGRAY);

        cardBox.getChildren().addAll(name, img, hpLabel, stats);

        if (isDead) {
            ColorAdjust gray = new ColorAdjust();
            gray.setSaturation(-1);
            gray.setBrightness(-0.6);
            cardBox.setEffect(gray);
        }

        container.getChildren().addAll(lblOwner, cardBox);
        return container;
    }
}
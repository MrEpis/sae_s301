package app.views;

import app.controller.MainController;
import app.model.Card;
import app.model.FightResultModel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
    private final Card myCard; // Ma carte mise à jour

    public FightResultView(Stage stage, MainController controller, FightResultModel result, Card myCard) {
        this.stage = stage;
        this.controller = controller;
        this.result = result;
        this.myCard = myCard;
    }

    public void show() {
        VBox root = new VBox(25);
        root.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #D32F2F; -fx-border-width: 5;");
        root.setAlignment(Pos.CENTER);

        Label title = new Label("RAPPORT DE COMBAT");
        title.setTextFill(Color.web("#FF5252"));
        title.setFont(Font.font("Arial", FontWeight.BLACK, 32));

        Label logLabel = new Label(result.getLogMessage());
        logLabel.setTextFill(Color.web("#FFC107")); // Jaune
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        logLabel.setWrapText(true);
        logLabel.setTextAlignment(TextAlignment.CENTER);
        logLabel.setMaxWidth(600);

        HBox arena = new HBox(60);
        arena.setAlignment(Pos.CENTER);

        VBox mySide;
        if (myCard != null) {
            boolean amIDead = myCard.getHp() <= 0;
            mySide = createFighterCard(myCard, "Moi", amIDead, Color.GREEN);
        } else {
            mySide = new VBox(new Label("?")); // Cas où on a perdu l'ID
        }

        Label vs = new Label("VS");
        vs.setTextFill(Color.GRAY);
        vs.setFont(Font.font("Arial", FontWeight.BLACK, 40));

        Card oppCard = result.getOpponentCard();
        boolean isOppDead = (oppCard != null && oppCard.getHp() <= 0);
        VBox oppSide = createFighterCard(oppCard, "Adversaire", isOppDead, Color.RED);

        arena.getChildren().addAll(mySide, vs, oppSide);

        Button closeBtn = new Button("Fermer le rapport");
        closeBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        closeBtn.setPrefSize(200, 40);
        closeBtn.setOnAction(e -> controller.showNotifications());

        root.getChildren().addAll(title, logLabel, arena, closeBtn);

        Scene scene = new Scene(root, 900, 650);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createFighterCard(Card card, String ownerTitle, boolean isDead, Color themeColor) {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);

        Label lblOwner = new Label(ownerTitle);
        lblOwner.setTextFill(themeColor);
        lblOwner.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        if (card == null) return container; // Sécurité

        VBox cardBox = new VBox(5);
        cardBox.setPrefSize(180, 260);
        cardBox.setAlignment(Pos.TOP_CENTER);

        String borderColor = isDead ? "#555555" : themeColor.toString().replace("0x", "#");
        cardBox.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: " + borderColor + "; -fx-border-width: 3; -fx-border-radius: 8;");

        Label name = new Label(card.getNom());
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        ImageView img = new ImageView();
        img.setFitWidth(100); img.setFitHeight(100); img.setPreserveRatio(true);
        try {
            if (card.getImagePath() != null) {
                File f = new File(card.getImagePath());
                if(f.exists()) img.setImage(new Image(f.toURI().toString()));
            }
        } catch(Exception e){}

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
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

/**
 * View responsible for displaying the outcome of a combat.
 * It presents a visual summary of the duel, showing the state of both cards
 * (or placeholders if they were destroyed) and the detailed combat log.
 */
public class FightResultView {

    private final Stage stage;
    private final MainController controller;
    private final FightResultModel result;
    private final Card myCard;

    /**
     * Initializes the FightResultView with the combat result data.
     * @param stage The stage window to display the result.
     * @param controller The main controller for navigation.
     * @param result The model containing the combat log and opponent's final card state.
     * @param myCard The local player's card state after the fight.
     */
    public FightResultView(Stage stage, MainController controller, FightResultModel result, Card myCard) {
        this.stage = stage;
        this.controller = controller;
        this.result = result;
        this.myCard = myCard;
    }

    /**
     * Constructs the scene and displays the combat report window.
     * It handles the logic to show the fighters or a "destroyed" message if cards are missing.
     */
    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #D32F2F; -fx-border-width: 5;");

        Label pseudoLabel = new Label("Utilisateur : " + controller.getLocalPlayer().getName());
        pseudoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pseudoLabel.setTextFill(Color.web("#A97DDE"));

        Label title = new Label("RAPPORT DE COMBAT");
        title.setTextFill(Color.web("#FF5252"));
        title.setFont(Font.font("Arial", FontWeight.BLACK, 32));

        StackPane header = new StackPane(title, pseudoLabel);
        StackPane.setAlignment(pseudoLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(title, Pos.CENTER);
        root.setTop(header);

        VBox centerBox = new VBox(50);
        centerBox.setAlignment(Pos.CENTER);

        Label logLabel = new Label(result.getLogMessage());
        logLabel.setTextFill(Color.web("#FFC107"));
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        logLabel.setWrapText(true);
        logLabel.setTextAlignment(TextAlignment.CENTER);
        logLabel.setAlignment(Pos.CENTER);
        logLabel.setMaxWidth(750);
        logLabel.setPrefWidth(750);

        centerBox.getChildren().add(logLabel);

        if (result.getOpponentCard() != null || myCard != null) {
            HBox arena = new HBox(60);
            arena.setAlignment(Pos.CENTER);

            VBox mySide = (myCard != null)
                    ? createFighterCard(myCard, "Moi", myCard.getHp() <= 0, Color.GREEN)
                    : createDestroyedPlaceholder("Ma carte est détruite");

            Label vs = new Label("VS");
            vs.setTextFill(Color.GRAY);
            vs.setFont(Font.font("Arial", FontWeight.BLACK, 40));

            VBox oppSide = (result.getOpponentCard() != null)
                    ? createFighterCard(result.getOpponentCard(), "Adversaire", result.getOpponentCard().getHp() <= 0, Color.RED)
                    : createDestroyedPlaceholder("Adversaire détruit");

            arena.getChildren().addAll(mySide, vs, oppSide);
            centerBox.getChildren().add(arena);
        } else {
            Label allDestroyed = new Label("LES DEUX CHAMPIONS ONT ÉTÉ DÉTRUITS DANS LE CHOC !");
            allDestroyed.setTextFill(Color.web("#FF5252"));
            allDestroyed.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            centerBox.getChildren().add(allDestroyed);
        }

        Button closeBtn = new Button("Fermer le rapport");
        closeBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 5;");
        closeBtn.setPrefSize(250, 50);
        closeBtn.setOnAction(e -> controller.showNotifications());

        centerBox.getChildren().add(closeBtn);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 900, 650);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    /**
     * Creates a visual representation of a fighter card with its final status and stats.
     * @param card The Card object to display.
     * @param ownerTitle Label for the card owner (e.g., "Me" or "Opponent").
     * @param isDead True if the card reached 0 HP during the fight.
     * @param themeColor The primary color for the card's theme.
     * @return A VBox containing the card's visual identity and stats.
     */
    private VBox createFighterCard(Card card, String ownerTitle, boolean isDead, Color themeColor) {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);

        Label lblOwner = new Label(ownerTitle);
        lblOwner.setTextFill(themeColor);
        lblOwner.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        VBox cardBox = new VBox(5);
        cardBox.setPrefSize(200, 280);
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


        Label stats = new Label("HP: " + card.getHp() + " | ATK: " + card.getAtk() + " | DEF: " + card.getDef());
        stats.setTextFill(Color.LIGHTGRAY);
        stats.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        cardBox.getChildren().addAll(name, img, stats);

        if (isDead) {
            ColorAdjust gray = new ColorAdjust();
            gray.setSaturation(-1);
            gray.setBrightness(-0.6);
            cardBox.setEffect(gray);
        }

        container.getChildren().addAll(lblOwner, cardBox);
        return container;
    }

    /**
     * Creates a placeholder widget for when a card has been destroyed and is no longer available.
     * @param message The message to display within the placeholder.
     * @return A styled VBox representing the missing card.
     */
    private VBox createDestroyedPlaceholder(String message) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(180, 260);
        box.setStyle("-fx-background-color: #331111; -fx-border-color: #555; -fx-border-width: 2; -fx-border-style: dashed;");

        Label l = new Label(message);
        l.setTextFill(Color.GRAY);
        l.setWrapText(true);
        l.setTextAlignment(TextAlignment.CENTER);

        box.getChildren().add(l);
        return box;
    }
}
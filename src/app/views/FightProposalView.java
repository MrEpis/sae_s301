package app.views;

import app.controller.MainController;
import app.model.Card;
import app.model.TradeRequestModel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.File;

public class FightProposalView {

    private final Stage stage;
    private final MainController controller;
    private final TradeRequestModel request;
    private VBox remoteCardContainer;

    public FightProposalView(Stage stage, MainController controller, TradeRequestModel request) {
        this.stage = stage;
        this.controller = controller;
        this.request = request;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #222222; -fx-border-color: #D32F2F; -fx-border-width: 4;");
        root.setAlignment(Pos.CENTER);

        Label title = new Label("DÉFI DE COMBAT !");
        title.setTextFill(Color.web("#FF5252"));
        title.setFont(Font.font("Arial", FontWeight.BLACK, 28));

        Label subtitle = new Label("Défié par : " + request.getInitiatorUsername());
        subtitle.setTextFill(Color.WHITE);
        subtitle.setFont(Font.font("Arial", 16));

        HBox cardsBox = new HBox(40);
        cardsBox.setAlignment(Pos.CENTER);

        remoteCardContainer = new VBox();
        updateRemoteCardDisplay(null);

        Card myCard = null;
        for(Card c : controller.getLocalPlayer().getInventory()) {
            if(c.getId() == request.getReceiverCardId()) {
                myCard = c;
                break;
            }
        }
        // Widget vert pour ma carte
        VBox myCardDisplay = createCardWidget(myCard, "Votre Champion", "#4CAF50");

        cardsBox.getChildren().addAll(remoteCardContainer, myCardDisplay);

        HBox buttonsBox = new HBox(20);
        buttonsBox.setAlignment(Pos.CENTER);

        Button acceptBtn = createButton("COMBATTRE !", "#D32F2F"); // Rouge sang
        acceptBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        acceptBtn.setOnAction(e -> {
            controller.respondToFight(request, true, controller.getLocalPlayer().getId_Client());
        });

        Button refuseBtn = createButton("FUIR", "#757575"); // Gris
        refuseBtn.setOnAction(e -> {
            controller.respondToFight(request, false, controller.getLocalPlayer().getId_Client());
        });

        Button backBtn = createButton("RETOUR", "#444");
        backBtn.setOnAction(e -> controller.showNotifications());

        buttonsBox.getChildren().addAll(acceptBtn, refuseBtn, backBtn);

        root.getChildren().addAll(title, subtitle, cardsBox, buttonsBox);

        controller.fetchRemoteCardForTrade(request, this);

        Scene scene = new Scene(root, 750, 550);
        stage.setScene(scene);
        stage.show();
    }

    private Button createButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btn.setPrefSize(140, 45);
        return btn;
    }

    // Méthode adaptée pour être appelée par le contrôleur (similaire à TradeProposalView)
    public void updateRemoteCardDisplay(Card card) {
        remoteCardContainer.getChildren().clear();
        if (card == null) {
            VBox placeholder = new VBox(new Label("Chargement de l'ennemi..."));
            placeholder.setPrefSize(160, 240);
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setStyle("-fx-border-color: #FF5252; -fx-border-width: 2; -fx-border-style: dashed;");
            ((Label)placeholder.getChildren().get(0)).setTextFill(Color.WHITE);
            remoteCardContainer.getChildren().add(placeholder);
        } else {
            remoteCardContainer.getChildren().add(createCardWidget(card, "Adversaire", "#FF5252"));
        }
    }

    private VBox createCardWidget(Card card, String title, String borderColor) {
        VBox box = new VBox(5);
        box.setPrefSize(160, 240);
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("-fx-background-color: #333; -fx-border-color: " + borderColor + "; -fx-border-width: 3; -fx-border-radius: 5;");
        if (card != null) {
            Label titleLbl = new Label(title); titleLbl.setTextFill(Color.web(borderColor)); titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            Label nameLbl = new Label(card.getNom()); nameLbl.setTextFill(Color.WHITE); nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            ImageView imgView = new ImageView(); imgView.setFitWidth(100); imgView.setFitHeight(100); imgView.setPreserveRatio(true);
            try { if (card.getImagePath() != null) { File file = new File(card.getImagePath()); if(file.exists()) imgView.setImage(new Image(file.toURI().toString())); } } catch(Exception e) {}
            box.getChildren().addAll(titleLbl, nameLbl, imgView, new Label("ATK: "+card.getAtk()), new Label("HP: "+card.getHp()));
            // Styliser les labels stats si besoin
            box.getChildren().forEach(n -> { if(n instanceof Label) ((Label)n).setTextFill(Color.WHITE); });
            titleLbl.setTextFill(Color.web(borderColor)); // Restaurer couleur titre
        }
        return box;
    }
}
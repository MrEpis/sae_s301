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

public class TradeProposalView {

    private final Stage stage;
    private final MainController controller;
    private final TradeRequestModel request;
    private VBox remoteCardContainer;

    public TradeProposalView(Stage stage, MainController controller, TradeRequestModel request) {
        this.stage = stage;
        this.controller = controller;
        this.request = request;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #2e2e2e;");
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Proposition d'échange");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Label subtitle = new Label("Proposé par : " + request.getInitiatorUsername());
        subtitle.setTextFill(Color.web("#A97DDE"));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

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
        VBox myCardDisplay = createCardWidget(myCard, "Votre Carte", "#4CAF50");

        cardsBox.getChildren().addAll(remoteCardContainer, myCardDisplay);

        HBox buttonsBox = new HBox(20);
        buttonsBox.setAlignment(Pos.CENTER);

        Button acceptBtn = createButton("ACCEPTER", "#4CAF50");
        acceptBtn.setOnAction(e -> {
            controller.respondToTrade(request, true, controller.getLocalPlayer().getId_Client());
        });

        Button refuseBtn = createButton("REFUSER", "#F44336");
        refuseBtn.setOnAction(e -> {
            controller.respondToTrade(request, false, controller.getLocalPlayer().getId_Client());
        });

        Button backBtn = createButton("RETOUR", "#9E9E9E");
        backBtn.setOnAction(e -> controller.showNotifications());

        buttonsBox.getChildren().addAll(acceptBtn, refuseBtn, backBtn);

        root.getChildren().addAll(title, subtitle, cardsBox, buttonsBox);

        controller.fetchRemoteCardForTrade(request, this);

        Scene scene = new Scene(root, 700, 550);
        stage.setScene(scene);
        stage.show();
    }

    private Button createButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setPrefSize(120, 40);
        return btn;
    }

    public void updateRemoteCardDisplay(Card card) {
        remoteCardContainer.getChildren().clear();
        if (card == null) {
            VBox placeholder = new VBox(new Label("Chargement..."));
            placeholder.setPrefSize(160, 240);
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setStyle("-fx-border-color: #F44336; -fx-border-width: 2; -fx-border-style: dashed;");
            ((Label)placeholder.getChildren().get(0)).setTextFill(Color.WHITE);
            remoteCardContainer.getChildren().add(placeholder);
        } else {
            remoteCardContainer.getChildren().add(createCardWidget(card, "Carte Adverse", "#F44336"));
        }
    }

    private VBox createCardWidget(Card card, String title, String borderColor) {
        VBox box = new VBox(5);
        box.setPrefSize(160, 240);
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("-fx-background-color: #333333; -fx-border-color: " + borderColor + "; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-radius: 5;");
        if (card != null) {
            Label titleLbl = new Label(title); titleLbl.setTextFill(Color.web(borderColor)); titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            Label nameLbl = new Label(card.getNom()); nameLbl.setTextFill(Color.WHITE); nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            ImageView imgView = new ImageView(); imgView.setFitWidth(100); imgView.setFitHeight(100); imgView.setPreserveRatio(true);
            try { if (card.getImagePath() != null) { File file = new File(card.getImagePath()); if(file.exists()) imgView.setImage(new Image(file.toURI().toString())); } } catch(Exception e) {}
            box.getChildren().addAll(titleLbl, nameLbl, imgView, createStatLabel("ATK: "+card.getAtk(), "#F44336"), createStatLabel("DEF: "+card.getDef(), "#2196F3"), createStatLabel("HP: "+card.getHp(), "#4CAF50"));
        } else { box.getChildren().add(new Label("Carte introuvable")); }
        return box;
    }
    private Label createStatLabel(String text, String color) { Label l = new Label(text); l.setTextFill(Color.web(color)); l.setFont(Font.font("Arial", FontWeight.BOLD, 14)); return l; }
}
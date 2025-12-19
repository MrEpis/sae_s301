package app.views;

import app.controller.MainController;
import app.model.TradeRequestModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class NotificationView {

    private final Stage primaryStage;
    private final MainController controller;
    private final List<TradeRequestModel> notifications;

    public NotificationView(Stage primaryStage, MainController controller, List<TradeRequestModel> notifications) {
        this.primaryStage = primaryStage;
        this.controller = controller;
        this.notifications = notifications;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #222222;");

        Label pseudoLabel = new Label("Utilisateur : " + controller.getLocalPlayer().getName());
        pseudoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pseudoLabel.setTextFill(Color.web("#A97DDE"));

        Label titleLabel = new Label("NOTIFICATIONS");
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 32));
        titleLabel.setTextFill(Color.web("#ffffff"));

        StackPane header = new StackPane(titleLabel, pseudoLabel);
        StackPane.setAlignment(pseudoLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(header);

        VBox notificationList = new VBox(15);
        notificationList.setAlignment(Pos.TOP_CENTER);
        notificationList.setPadding(new Insets(30, 0, 0, 0));

        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("Aucune notification pour le moment.");
            emptyLabel.setTextFill(Color.GRAY);
            notificationList.getChildren().add(emptyLabel);
        } else {
            for (TradeRequestModel req : notifications) {
                notificationList.getChildren().add(createNotificationItem(req));
            }
        }

        ScrollPane scrollPane = new ScrollPane(notificationList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setCenter(scrollPane);

        Button backBtn = new Button("Retour au Menu");
        backBtn.setPrefSize(200, 45);
        backBtn.setStyle("-fx-background-color: #D9C6F0; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> controller.showMenu());

        HBox bottomBox = new HBox(backBtn);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));
        root.setBottom(bottomBox);

        return new Scene(root, 850, 650);
    }

    private HBox createNotificationItem(TradeRequestModel req) {
        HBox item = new HBox(20);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: #333333; -fx-border-color: #555555; -fx-border-radius: 10;");

        String text;
        if (req.getFightResult() != null) {
            String name = (req.getInitiatorUsername() != null) ? req.getInitiatorUsername() : "Joueur " + req.getInitiatorId();
            text = "Résultat de combat contre " + name + " disponible";
        }else {
            text = (req.isFight() ? "Combat contre " : "Échange avec ") +
                    (req.getInitiatorUsername() != null ? req.getInitiatorUsername() : "Joueur " + req.getInitiatorId());
        }

        Label desc = new Label(text);
        desc.setTextFill(Color.WHITE);
        desc.setFont(Font.font("Arial", 16));
        HBox.setHgrow(desc, Priority.ALWAYS);

        Button actionBtn = new Button("Voir");
        actionBtn.setStyle("-fx-background-color: #A97DDE; -fx-text-fill: white; -fx-font-weight: bold;");
        actionBtn.setOnAction(e -> {
            if (req.getFightResult() != null) {
                new FightResultView(primaryStage, controller, req.getFightResult(), req.getFightResult().getMyCard()).show();
            } else if (req.isFight()) {
                new FightProposalView(primaryStage, controller, req).show();
            } else {
                new TradeProposalView(primaryStage, controller, req).show();
            }
        });

        item.getChildren().addAll(desc, actionBtn);
        return item;
    }

    public void show() {
        primaryStage.setScene(createScene());
        primaryStage.setTitle("Notifications");
        primaryStage.sizeToScene();
        primaryStage.show();
    }
}
package app.views;

import app.controller.MainController;
import app.model.FightResultModel;
import app.model.TradeRequestModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class NotificationView {

    private final Stage primaryStage;
    private final MainController controller;
    private final List<TradeRequestModel> requests;

    public NotificationView(Stage primaryStage, MainController controller, List<TradeRequestModel> requests) {
        this.primaryStage = primaryStage;
        this.controller = controller;
        this.requests = requests;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #222;");
        root.setPadding(new Insets(20));

        Label title = new Label("Notifications");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        ListView<TradeRequestModel> listView = new ListView<>();
        listView.getItems().addAll(requests);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(TradeRequestModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    if (item.isFightResult()) {
                        setText("🏆 Résultat du combat contre " + item.getInitiatorUsername());
                        setStyle("-fx-text-fill: #FFC107; -fx-font-size: 14px; -fx-padding: 10; -fx-font-weight: bold; -fx-border-color: #FFC107; -fx-border-width: 0 0 1 0;");
                    }
                    else if (item.isFight()) {
                        setText("⚔️ DÉFI DE COMBAT reçu de " + item.getInitiatorUsername());
                        setStyle("-fx-text-fill: #FF5252; -fx-font-size: 14px; -fx-padding: 10; -fx-font-weight: bold;");
                    }
                    else {
                        setText("🤝 Échange proposé par " + item.getInitiatorUsername());
                        setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10;");
                    }
                }
            }
        });

        listView.setStyle("-fx-background-color: #333; -fx-control-inner-background: #333;");

        listView.setOnMouseClicked(e -> {
            TradeRequestModel selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.isFightResult()) {
                    FightResultModel result = selected.getFightResult();
                    new FightResultView(primaryStage, controller, result, result.getMyCard()).show();
                }
                else if (selected.isFight()) {
                    new FightProposalView(primaryStage, controller, selected).show();
                }
                else {
                    new TradeProposalView(primaryStage, controller, selected).show();
                }
            }
        });

        root.setCenter(listView);

        Button backBtn = new Button("Retour au menu");
        backBtn.setStyle("-fx-background-color: #A97DDE; -fx-text-fill: black; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> controller.showMenu());
        BorderPane.setAlignment(backBtn, Pos.CENTER);
        BorderPane.setMargin(backBtn, new Insets(10));
        root.setBottom(backBtn);

        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setTitle("Mes Notifications");
        primaryStage.show();
    }
}
package app.views;

import app.controller.LoginController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginView {

    private final Stage primaryStage;
    private LoginController controller;
    private TextField usernameField;

    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setController(LoginController controller) {
        this.controller = controller;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2e2e2e;");

        Label title = new Label("Bienvenue !");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitle = new Label("Choisissez votre nom d'utilisateur");
        subtitle.setTextFill(Color.LIGHTGRAY);

        usernameField = new TextField();
        usernameField.setMaxWidth(300);
        usernameField.setPromptText("Pseudo...");

        usernameField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if(newText.length() > 20 || !newText.matches("[0-9a-zA-ZàâäéèêëîïôöùûüÿçÀÂÄÉÈÊËÎÏÔÖÙÛÜŸÇ_-]*")) {
                return null;
            }
            return change;
        }));

        Button validateButton = new Button("ça part !!!");
        validateButton.setStyle("-fx-background-color: #7834CB; -fx-text-fill: white; -fx-font-weight: bold;");
        validateButton.setOnAction(e -> {
            if (controller != null) {
                controller.handleFirstConnection(usernameField.getText());
            }
        });

        usernameField.setOnAction(e -> {
            if (controller != null) {
                controller.handleFirstConnection(usernameField.getText());
            }
        });

        root.getChildren().addAll(title, subtitle, usernameField, validateButton);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Première Connexion");
        primaryStage.show();
    }
}
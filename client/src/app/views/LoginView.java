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

/**
 * Entry point interface for user identification.
 * This view provides the initial form where users can select their username
 * and establish a connection with the game server.
 */
public class LoginView {

    private final Stage primaryStage;
    private LoginController controller;
    private TextField usernameField;
    private Label errorLabel;

    /**
     * Initializes the LoginView with the primary stage.
     * @param primaryStage The main application window stage.
     */
    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Updates the error label with a specific feedback message.
     * Typically used for server errors or capacity warnings.
     * @param message The error description to display to the user.
     */
    public void displayError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    /**
     * Renders the login form with username input and a validation button.
     * This method builds the layout, applies character and length constraints
     * (max 20 characters, alphanumeric and specific symbols), and sets up
     * action listeners for both the button and the Enter key.
     */
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

        // Enforces character and length constraints on the username field
        usernameField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if(newText.length() > 20 || !newText.matches("[0-9a-zA-ZàâäéèêëîïôöùûüÿçÀÂÄÉÈÊËÎÏÔÖÙÛÜŸÇ_-]*")) {
                return null;
            }
            return change;
        }));

        errorLabel = new Label("");
        errorLabel.setTextFill(Color.web("#FF5252"));
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

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

        root.getChildren().addAll(title, subtitle, usernameField, validateButton, errorLabel);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Première Connexion");
        primaryStage.show();
    }

    public void setController(LoginController controller) {
        this.controller = controller;
    }
}
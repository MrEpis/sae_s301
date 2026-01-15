package app.views;

import app.controller.BotController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Represents the graphical user interface for the automated bot manager ("roblobot").
 * This view displays the bot's current operational status and allows for its disconnection.
 */
public class BotView {

    private final Stage stage;
    private BotController controller;
    private Label statusLabel;

    /**
     * Constructs a new BotView.
     * @param stage The primary JavaFX stage for this view.
     */
    public BotView(Stage stage) {
        this.stage = stage;
    }

    /**
     * Initializes and displays the bot manager window.
     * Sets up the layout containing the title, status indicator, and the shutdown button.
     */
    public void show() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #111;");

        Label title = new Label("BOT ACTIVE");
        title.setTextFill(Color.LIMEGREEN);
        title.setFont(new Font("Arial", 24));

        statusLabel = new Label("En attente...");
        statusLabel.setTextFill(Color.WHITE);

        Button stopBtn = new Button("Déconnecter le Bot");
        stopBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");
        stopBtn.setOnAction(e -> {
            if (controller != null) controller.stop();
            stage.close();
            System.exit(0);
        });

        root.getChildren().addAll(title, statusLabel, stopBtn);

        Scene scene = new Scene(root, 300, 200);
        stage.setScene(scene);
        stage.setTitle("Roblobot Manager");
        stage.show();
    }

    public void setController(BotController controller) {
        this.controller = controller;
    }

    public void setStatus(String text) {
        if (statusLabel != null) statusLabel.setText(text);
    }
}
package app;

import app.controller.BotController;
import app.controller.MainController;
import app.views.BotView;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

/**
 * The main JavaFX Application class.
 * This class acts as the system's orchestrator, determining at startup whether
 * to launch the interactive graphical interface for a player or the automated
 * background logic for a bot.
 */
public class App extends Application {

    /**
     * Initializes the application and decides the execution mode based on command-line arguments.
     * <p>
     * If the arguments contain "bot", the application starts in <b>Bot Mode</b>,
     * initializing the BotView and BotController. Otherwise, it starts in <b>Standard Mode</b>
     * by handing control over to the MainController.
     * </p>
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        List<String> args = getParameters().getRaw();

        if (args.contains("bot")) {
            System.out.println("DÉMARRAGE MODE BOT");

            BotView botView = new BotView(primaryStage);
            BotController botController = new BotController(botView);
            botView.setController(botController);

            botView.show();

            new Thread(botController::start).start();

        } else {
            MainController mainController = new MainController(primaryStage);
            mainController.start();
        }
    }

    /**
     * Standard main method used to launch the JavaFX application.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
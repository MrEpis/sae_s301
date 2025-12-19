package app;

import app.controller.BotController;
import app.controller.MainController;
import app.views.BotView;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class App extends Application {

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

    public static void main(String[] args) {
        launch(args);
    }
}
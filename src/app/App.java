package app;

import app.controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // On instancie le chef d'orchestre (MainController)
        MainController mainController = new MainController(primaryStage);

        // On lance la logique de démarrage (vérification session, login, etc.)
        mainController.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package app.views;

import app.controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainView extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Crée le MainController et lance l'application
        MainController mainController = new MainController(primaryStage);
        mainController.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
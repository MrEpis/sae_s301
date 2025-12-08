package java.views;

import java.controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainView extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Crée le MainController et lance l'application
        MainController mainController = new MainController(primaryStage);
        mainController.showMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
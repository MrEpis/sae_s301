package views;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainView extends Application {

    @Override
    public void start(Stage primaryStage) {
        new MenuView(primaryStage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
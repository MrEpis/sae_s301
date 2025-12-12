package app.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Consumer;

public class ImageSelectorView {

    private final Stage parentStage;
    private final Consumer<File> onImageSelected;

    public ImageSelectorView(Stage parentStage, Consumer<File> onImageSelected) {
        this.parentStage = parentStage;
        this.onImageSelected = onImageSelected;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initOwner(parentStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Galerie d'images");

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #2e2e2e;");
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        TilePane tilePane = new TilePane();
        tilePane.setHgap(15);
        tilePane.setVgap(15);
        tilePane.setPrefColumns(4);
        tilePane.setAlignment(Pos.TOP_LEFT);
        tilePane.setStyle("-fx-background-color: #2e2e2e;");

        File folder = new File("src/ressources/img");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") ||
                            name.toLowerCase().endsWith(".jpg")
            );

            if (files != null) {
                for (File file : files) {
                    tilePane.getChildren().add(createImageItem(file, stage));
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2e2e2e; -fx-border-color: transparent; -fx-background-color: #2e2e2e;");

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().add(scrollPane);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        stage.setMinWidth(870);
        stage.setMinHeight(600);
        stage.setWidth(870);
        stage.setHeight(600);

        stage.show();
    }

    private VBox createImageItem(File file, Stage stage) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setSpacing(5);
        box.setStyle("-fx-background-color: #383838; -fx-border-color: #555; -fx-border-width: 2; -fx-border-radius: 8;");

        try {
            ImageView iv = new ImageView(new Image(file.toURI().toString()));
            iv.setFitWidth(100);
            iv.setFitHeight(100);
            iv.setPreserveRatio(true);
            box.getChildren().add(iv);
        } catch (Exception e) { }

        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #505050; -fx-border-color: #7834CB; -fx-border-width: 2; -fx-border-radius: 8; -fx-cursor: hand;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: #383838; -fx-border-color: #555; -fx-border-width: 2; -fx-border-radius: 8;"));

        box.setOnMouseClicked(e -> {
            onImageSelected.accept(file);
            stage.close();
        });

        return box;
    }
}
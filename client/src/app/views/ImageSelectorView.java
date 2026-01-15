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

/**
 * Gallery window allowing the user to select a local image for card creation.
 * It scans a specific resource directory and displays compatible image files
 * in a modal selection interface.
 */
public class ImageSelectorView {

    private final Stage parentStage;
    private final Consumer<File> onImageSelected;

    /**
     * Constructs the ImageSelectorView with a reference to the parent stage
     * and a callback for the selection event.
     * @param parentStage The owner stage of this modal window.
     * @param onImageSelected A consumer that processes the selected File object.
     */
    public ImageSelectorView(Stage parentStage, Consumer<File> onImageSelected) {
        this.parentStage = parentStage;
        this.onImageSelected = onImageSelected;
    }

    /**
     * Configures and displays the image gallery as a modal stage.
     * It initializes a TilePane layout, scans the "src/ressources/img" directory
     * for PNG and JPG files, and wraps the content in a ScrollPane for navigation.
     */
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

        // Loads all compatible image files from the local resource folder
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

        Scene scene = new Scene(root, 850, 600);
        stage.setScene(scene);

        stage.setMinWidth(850);
        stage.setMinHeight(600);
        stage.sizeToScene();

        stage.show();
    }

    /**
     * Creates a clickable preview box for a single image file.
     * This method handles the creation of the thumbnail, hover styling effects,
     * and the selection logic that triggers the callback and closes the window.
     * @param file The image file to be displayed.
     * @param stage The current modal stage to be closed upon selection.
     * @return A styled VBox containing the image preview.
     */
    private VBox createImageItem(File file, Stage stage) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setSpacing(5);
        box.setStyle("-fx-background-color: #383838; -fx-border-color: #555; -fx-border-width: 2; -fx-border-radius: 8;");

        try {
            Image img = new Image(file.toURI().toString(), 150, 150, true, true, true);


            ImageView iv = new ImageView(img);
            iv.setFitWidth(120);
            iv.setFitHeight(120);
            iv.setPreserveRatio(true);
            box.getChildren().add(iv);

        } catch (Exception e) {
            System.err.println("Erreur chargement image " + file.getName());
        }

        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #505050; -fx-border-color: #7834CB; -fx-border-width: 2; -fx-border-radius: 8; -fx-cursor: hand;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: #383838; -fx-border-color: #555; -fx-border-width: 2; -fx-border-radius: 8;"));

        // Executes the callback and closes the window when an image is selected
        box.setOnMouseClicked(e -> {
            onImageSelected.accept(file);
            stage.close();
        });

        return box;
    }
}
package app.controller;

import app.model.Player;
import app.views.CardCreationView;
import java.io.File;

public class CardCreationController {

    private final MainController mainController;
    private final Player player;
    private final CardCreationView creationView;
    private String selectedImagePath;

    public CardCreationController(MainController mainController, Player player, CardCreationView creationView) {
        this.mainController = mainController;
        this.player = player;
        this.creationView = creationView;
        this.selectedImagePath = null;
    }

    public void backToMenu() {
        mainController.showMenu();
    }

    public void chooseImageFile() {
        File selectedFile = creationView.openFileChooser();

        if (selectedFile != null) {
            this.selectedImagePath = selectedFile.toURI().toString();

            String fileName = selectedFile.getName();

            String cardName;
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                cardName = fileName.substring(0, lastDot);
            } else {
                cardName = fileName;
            }

            creationView.displayImagePreview(this.selectedImagePath, cardName);
        }
    }

    public void saveCard(String name, int hp, int atk, int def) {
        if (selectedImagePath == null) {
            System.err.println("Erreur: Veuillez sélectionner une image pour la carte.");
            return;
        }

        if ((hp + atk + def) > CardCreationView.MAX_POINTS) {
            System.err.println("Erreur: Total de points supérieur au maximum autorisé.");
            return;
        }

        System.out.println("Carte '" + name + "' créée et ajoutée à l'inventaire avec image.");

        this.selectedImagePath = null;
        backToMenu();
    }
}
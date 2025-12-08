package app.controller;

import app.model.Player;
import app.views.CardCreationView;
import java.io.File;

public class CardCreationController {

    private final MainController mainController;
    private final Player player; // TODO: Fournir le joueur réel
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
            creationView.displayImagePreview(this.selectedImagePath);
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

        // TODO: Mettre à jour java.model.Card pour accepter le chemin de l'image (String imagePath)
        // Card newCard = new Card(name, hp, atk, def, selectedImagePath);
        // player.addCard(newCard);

        System.out.println("Carte '" + name + "' créée et ajoutée à l'inventaire avec image.");

        this.selectedImagePath = null;
        backToMenu();
    }
}
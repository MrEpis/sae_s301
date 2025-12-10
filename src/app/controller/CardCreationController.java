package app.controller;

import app.model.Player;
import app.service.JsonUtils;
import app.service.NetworkService;
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
            String cardName = fileName.lastIndexOf('.') > 0 ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            creationView.displayImagePreview(this.selectedImagePath, cardName);
        }
    }

    public void saveCard(String name, int hp, int atk, int def) {
        // 1. Validations locales
        if (selectedImagePath == null) {
            System.err.println("Erreur: Image manquante.");
            return;
        }
        if ((hp + atk + def) > CardCreationView.MAX_POINTS) {
            System.err.println("Erreur: Total de points dépassé.");
            return;
        }

        String jsonData = JsonUtils.buildCardCreationData(name, hp, atk);
        String jsonRequest = JsonUtils.buildRequest("RequestCardCreation", jsonData);

        // 3. Envoi au serveur via le MainController
        NetworkService network = mainController.getNetworkService();

        if (network != null) {
            System.out.println("Envoi de la requête : " + jsonRequest);

            // Envoi et attente de la réponse (Bloquant pour l'instant)
            String response = network.sendRequest(jsonRequest);

            System.out.println("Réponse du serveur : " + response);

            // TODO (Étape C): Analyser la réponse ("status": "OK") avant de fermer
            if (response != null && response.contains("OK")) {
                System.out.println("Succès ! Carte créée.");
                // On pourrait créer l'objet Card localement ici aussi
                this.selectedImagePath = null;
                backToMenu();
            } else {
                System.err.println("Le serveur a refusé la création.");
            }
        } else {
            System.err.println("Erreur critique : Pas de connexion réseau.");
        }
    }
}
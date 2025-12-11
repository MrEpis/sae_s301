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

    private File rawSelectedFile;

    public CardCreationController(MainController mainController, Player player, CardCreationView creationView) {
        this.mainController = mainController;
        this.player = player;
        this.creationView = creationView;
    }

    public void backToMenu() {
        mainController.showMenu();
    }

    public void chooseImageFile() {
        File selectedFile = creationView.openFileChooser();
        if (selectedFile != null) {
            this.rawSelectedFile = selectedFile;
            String displayURI = selectedFile.toURI().toString();

            String fileName = selectedFile.getName();
            String cardName = fileName.lastIndexOf('.') > 0 ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

            creationView.displayImagePreview(displayURI, cardName);
        }
    }

    public void saveCard(String name, int hp, int atk, int def) {
        if (rawSelectedFile == null) {
            System.err.println("Erreur: Image manquante.");
            return;
        }
        if ((hp + atk + def) > CardCreationView.MAX_POINTS) {
            System.err.println("Erreur: Total de points dépassé.");
            return;
        }

        String serverImagePath = "src/ressources/img/" + rawSelectedFile.getName();

        String jsonData = JsonUtils.buildCardCreationData(name, hp, atk, def, serverImagePath);
        String jsonRequest = JsonUtils.buildRequest("RequestCardCreation", jsonData);

        NetworkService network = mainController.getNetworkService();

        if (network != null) {
            System.out.println("Envoi : " + jsonRequest);
            String response = network.sendRequest(jsonRequest);
            System.out.println("Réponse : " + response);

            if (response != null && response.contains("OK")) {
                System.out.println("Succès ! Carte créée.");
                this.rawSelectedFile = null;
                backToMenu();
            } else {
                System.err.println("Échec création carte.");
            }
        }
    }
}
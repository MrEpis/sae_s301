package app.controller;

import app.model.Card; // NOUVEAU : Nécessaire pour créer l'objet Card
import app.model.Player;
import app.service.JsonUtils;
import app.service.NetworkService;
import app.service.SessionService;
import app.views.CardCreationView;
import app.views.ImageSelectorView;
import javafx.stage.Stage;

import java.io.File;
import java.util.regex.Matcher; // NOUVEAU
import java.util.regex.Pattern; // NOUVEAU

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
        Stage stage = (Stage) creationView.getScene().getWindow();

        ImageSelectorView selector = new ImageSelectorView(stage, selectedFile -> {
            if (selectedFile != null) {
                this.rawSelectedFile = selectedFile;
                String displayURI = selectedFile.toURI().toString();

                String fileName = selectedFile.getName();
                String defaultCardName = fileName.lastIndexOf('.') > 0 ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

                String currentInput = creationView.getCardNameInput();
                String finalName;

                if (currentInput == null || currentInput.trim().isEmpty()) {
                    finalName = defaultCardName;
                } else {
                    finalName = currentInput;
                }

                creationView.displayImagePreview(displayURI, finalName);
            }
        });

        selector.show();
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
        int clientId = SessionService.loadClientId();
        String jsonData = JsonUtils.buildCardCreationData(clientId, name, hp, atk, def, serverImagePath);
        String jsonRequest = JsonUtils.buildRequest("RequestCardCreation", jsonData);

        NetworkService network = mainController.getNetworkService();

        if (network != null) {
            System.out.println("Envoi : " + jsonRequest);
            String response = network.sendRequest(jsonRequest);
            System.out.println("Réponse : " + response);

            if (response != null && response.contains("OK")) {
                System.out.println("Succès ! Carte créée.");


                int newCardId = -1;
                Pattern pattern = Pattern.compile("\"id\":\\s*(\\d+)");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    newCardId = Integer.parseInt(matcher.group(1));
                }

                if (newCardId != -1) {
                    Card newCard = new Card(newCardId, name, hp, def, atk, serverImagePath);

                    player.addCard(newCard);
                    System.out.println("Carte (ID: " + newCardId + ") ajoutée à l'inventaire local.");
                } else {
                    System.err.println("Attention : Impossible de récupérer l'ID de la nouvelle carte.");
                }

                this.rawSelectedFile = null;
                backToMenu();
            } else {
                System.err.println("Échec création carte.");
            }
        }
    }
}
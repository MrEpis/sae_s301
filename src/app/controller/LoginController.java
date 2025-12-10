package app.controller;

import app.service.NetworkService;
import app.service.JsonUtils;
import app.service.SessionService;
import app.views.LoginView;

public class LoginController {

    private final MainController mainController;
    private final LoginView view;

    public LoginController(MainController mainController, LoginView view) {
        this.mainController = mainController;
        this.view = view;
    }

    public void handleFirstConnection(String username) {
        if (username == null || username.trim().isEmpty()) return;

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            // 1. Construire la requête (ID = 0 pour nouveau)
            String jsonData = JsonUtils.buildLoginData(0, username);
            String request = JsonUtils.buildRequest("LOGIN", jsonData);

            // 2. Envoyer
            String response = net.sendRequest(request);

            // 3. Traiter la réponse (Simulation basique pour BUT2)
            // On suppose que le serveur répond un JSON contenant l'ID, ex: ... "data": 42 ...
            if (response != null && response.contains("OK")) {
                System.out.println("Connexion réussie !");

                // TODO: Parser proprement le JSON pour récupérer le vrai ID envoyé par le serveur
                // Pour l'instant, on simule que le serveur nous a donné l'ID 101
                int newId = 101;

                // 4. Sauvegarder et passer au menu
                SessionService.saveClientId(newId);
                mainController.getLocalPlayer().setId(newId); // Mettre à jour le joueur en mémoire
                mainController.getLocalPlayer().setName(username);

                mainController.showMenu();
            } else {
                System.err.println("Erreur de connexion serveur");
            }
        }
    }
}
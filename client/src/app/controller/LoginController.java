package app.controller;

import app.model.Card;
import app.service.NetworkService;
import app.service.JsonUtils;
import app.service.SessionService;
import app.views.LoginView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller responsible for managing the initial user login process.
 * It handles communication with the server to validate usernames, retrieve client IDs,
 * and initialize the player's session and inventory.
 */
public class LoginController {

    private final MainController mainController;
    private final LoginView view;

    /**
     * Initializes the LoginController with the application's main controller and the login view.
     * @param mainController The central controller of the application.
     * @param view The graphical view for the login screen.
     */
    public LoginController(MainController mainController, LoginView view) {
        this.mainController = mainController;
        this.view = view;
    }

    /**
     * Processes a connection attempt for a first-time user or an unauthenticated session.
     * It sends a LOGIN request to the server, handles the response to extract the client ID
     * and inventory, saves the session locally, and transitions to the main menu upon success.
     * It also manages error displays for cases like full server capacity.
     * @param username The username provided by the user.
     */
    public void handleFirstConnection(String username) {
        if (username == null || username.trim().isEmpty()) return;

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            String jsonData = JsonUtils.buildLoginData(0, username);
            String request = JsonUtils.buildRequest("LOGIN", jsonData);

            String response = net.sendRequest(request);

            if (response != null && response.contains("OK")) {
                System.out.println("Connexion réussie !");

                Pattern pattern = Pattern.compile("\"id_client\":\\s*(\\d+)");
                Matcher matcher = pattern.matcher(response);
                int newId = 0;
                if (matcher.find()) {
                    newId = Integer.parseInt(matcher.group(1));
                }

                List<Card> inventory = JsonUtils.parseInventoryFromLogin(response);
                System.out.println(inventory.size() + " cartes récupérées.");

                SessionService.saveClientId(newId);
                mainController.getLocalPlayer().setId(newId);
                mainController.getLocalPlayer().setName(username);

                mainController.getLocalPlayer().getInventory().clear();
                mainController.getLocalPlayer().getInventory().addAll(inventory);

                mainController.showMenu();
            } else {
                if (response != null && response.contains("Server at max capacity")) {
                    view.displayError("Impossible de se connecter, serveur plein.");
                } else {
                    view.displayError("Erreur de connexion serveur");
                }
                System.err.println("Erreur de connexion serveur");
            }
        }
    }
}
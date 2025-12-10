package app.controller;

import app.service.NetworkService;
import app.service.JsonUtils;
import app.service.SessionService;
import app.views.LoginView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                SessionService.saveClientId(newId);
                mainController.getLocalPlayer().setId(newId);
                mainController.getLocalPlayer().setName(username);

                mainController.showMenu();
            } else {
                System.err.println("Erreur de connexion serveur");
            }
        }
    }
}
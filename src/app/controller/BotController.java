package app.controller;

import app.model.Card;
import app.model.Player;
import app.model.TradeRequestModel;
import app.service.JsonUtils;
import app.service.NetworkService;
import app.views.BotView;
import javafx.application.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotController {

    private final BotView view;
    private final NetworkService networkService;
    private final Player botPlayer;
    private final Random random = new Random();

    private final List<String> availableImages = new ArrayList<>();

    public BotController(BotView view) {
        this.view = view;
        this.networkService = new NetworkService();
        this.botPlayer = new Player(0, "roblobot");

        this.networkService.setNotificationListener(this::handleNotification);
    }

    private void loadImagesFromFolder() {
        try {
            File folder = new File("src/ressources/img");

            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".png"));

                if (files != null) {
                    for (File file : files) {
                        availableImages.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BOT] Erreur lecture images : " + e.getMessage());
        }

        if (availableImages.isEmpty()) {
            System.err.println("[BOT] Attention : Aucune image trouvée dans src/ressources/img !");
            availableImages.add("acorn.png");
        } else {
            System.out.println("[BOT] " + availableImages.size() + " images chargées.");
        }
    }

    public void start() {
        view.setStatus("Connexion au serveur...");

        String loginData = "{\"id_client\": 0, \"username\": \"roblobot\"}";
        String request = JsonUtils.buildRequest("LOGIN", loginData);

        String resp = networkService.sendRequest(request);

        if (resp != null && resp.contains("OK")) {
            int realId = extractIdFromResponse(resp);

            if (realId != -1) {
                botPlayer.setId(realId);
                botPlayer.setName("roblobot");

                List<Card> inv = JsonUtils.parseInventoryFromLogin(resp);
                botPlayer.getInventory().clear();
                botPlayer.getInventory().addAll(inv);

                Platform.runLater(() -> view.setStatus("Connecté (ID: " + realId + ")"));

                checkAndRefillInventory();
            } else {
                Platform.runLater(() -> view.setStatus("Erreur: ID non reçu du serveur"));
            }

        } else {
            Platform.runLater(() -> view.setStatus("Echec de connexion"));
        }
    }

    public void stop() {
        networkService.closeConnection();
    }

    private void handleNotification(String message) {
        System.out.println("[BOT] Reçu : " + message);

        if (message.contains("FightConfirmationRequest")) {
            TradeRequestModel req = JsonUtils.parseFightRequestNotification(message);
            Platform.runLater(() -> view.setStatus("Combat accepté contre " + req.getInitiatorId()));

            String jsonResp = JsonUtils.buildFightResponseJson(true, req, botPlayer.getId_Client());
            networkService.sendMessage(JsonUtils.buildResponse("ResponseFightRequest", jsonResp));
        }
        else if (message.contains("ConfirmationRequest")) {
            TradeRequestModel req = JsonUtils.parseTradeRequestNotification(message);
            Platform.runLater(() -> view.setStatus("Échange accepté avec " + req.getInitiatorId()));

            String jsonResp = JsonUtils.buildTradeResponseJson(true, req, botPlayer.getId_Client());
            networkService.sendMessage(JsonUtils.buildResponse("ConfirmationResponse", jsonResp));
        }
        else if (message.contains("FightResult") || message.contains("TradeResult")) {
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) {}

                if (message.contains("hand")) {
                    List<Card> newInv = JsonUtils.parseInventoryFromTradeResult(message);
                    botPlayer.getInventory().clear();
                    botPlayer.getInventory().addAll(newInv);
                }

                checkAndRefillInventory();
            }).start();
        }
    }

    private void checkAndRefillInventory() {
        int currentCount = botPlayer.getInventory().size();
        int needed = 4 - currentCount;

        if (needed > 0) {
            Platform.runLater(() -> view.setStatus("Génération de " + needed + " carte(s)..."));
            for (int i = 0; i < needed; i++) {
                createRandomCard();
                try { Thread.sleep(500); } catch (Exception e) {}
            }
            Platform.runLater(() -> view.setStatus("Prêt. Inventaire complet (4 cartes)."));
        } else {
            Platform.runLater(() -> view.setStatus("Prêt. En attente de défis..."));
        }
    }

    private void createRandomCard() {
        String name = "BotCard-" + random.nextInt(1000);
        int hp = 1 + random.nextInt(99);
        int atk = random.nextInt(99-hp);
        int def = random.nextInt(99-hp-atk);
        hp += 99 - hp - atk - def;
        String imgName = availableImages.get(random.nextInt(availableImages.size()));
        String imgPath = "src/ressources/img/" + imgName;

        String jsonPayload = JsonUtils.buildCardCreationData(
                botPlayer.getId_Client(),
                name,
                hp,
                atk,
                def,
                imgPath
        );

        String req = JsonUtils.buildRequest("RequestCardCreation", jsonPayload);

        String resp = networkService.sendRequest(req);
        System.out.println("[BOT] Création carte : " + resp);
    }

    private int extractIdFromResponse(String json) {
        try {
            Pattern p = Pattern.compile("\"id_client\"\\s*:\\s*(\\d+)");
            Matcher m = p.matcher(json);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
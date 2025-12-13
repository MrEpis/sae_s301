package app.controller;

import app.model.Card;
import app.model.TradeRequestModel;
import app.service.JsonUtils;
import app.service.SessionService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import app.views.*;
import app.model.Player;
import app.service.NetworkService;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    private final Stage primaryStage;
    private final Player localPlayer;

    private CombatController combatController;
    private CardCreationController cardCreationController;
    private InventoryController inventoryController;
    private TradeController tradeController;
    private LoginController loginController;

    private CardCreationView cardCreationView;
    private MenuView menuView;
    private NetworkService networkService;
    private final List<TradeRequestModel> notifications = new ArrayList<>();

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.networkService = new NetworkService();
        this.localPlayer = new Player(0, "Inconnu");

        this.networkService.setNotificationListener(this::handleNotification);

        this.menuView = new MenuView(primaryStage, this);
        this.cardCreationView = new CardCreationView(primaryStage);
        this.cardCreationController = new CardCreationController(this, localPlayer, cardCreationView);
        this.cardCreationView.setController(this.cardCreationController);

        this.primaryStage.setOnCloseRequest(event -> {
            quit();
            System.exit(0);
        });
    }

    private void showToast(String message) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            Label label = new Label(message);
            label.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #A97DDE; -fx-border-radius: 10; -fx-border-width: 2;");

            popup.getContent().add(label);
            popup.setAutoHide(true);

            if (primaryStage.isShowing()) {
                double x = primaryStage.getX() + primaryStage.getWidth() - 250;
                double y = primaryStage.getY() + 50;
                popup.show(primaryStage, x, y);
            }

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }

    private void handleNotification(String message) {
        System.out.println("Notification reçue : " + message);

        if (message.contains("ConfirmationRequest")) {
            TradeRequestModel req = JsonUtils.parseTradeRequestNotification(message);
            notifications.add(req);
            Platform.runLater(() -> primaryStage.setTitle("Robs Card Game - (" + notifications.size() + ") Notification(s) !"));
            showToast("Nouvelle demande d'échange !");
        }

        else if (message.contains("TradeResult") && message.contains("OK")) {
            List<Card> newInventory = JsonUtils.parseInventoryFromTradeResult(message);

            if (!newInventory.isEmpty()) {
                localPlayer.getInventory().clear();
                localPlayer.getInventory().addAll(newInventory);

                System.out.println("Inventaire mis à jour après échange (" + newInventory.size() + " cartes).");
                showToast("Échange validé ! Inventaire mis à jour.");
            }
        }
    }

    public void fetchRemoteCardForTrade(TradeRequestModel request, TradeProposalView view) {
        new Thread(() -> {
            String opponentUsername = getUsernameById(request.getInitiatorId());

            if (opponentUsername == null) {
                opponentUsername = getUsernameById(request.getInitiatorId());
            }

            if (opponentUsername != null) {
                String dataJson = JsonUtils.buildGetOpponentInventoryRequest(opponentUsername);
                String req = JsonUtils.buildRequest("GET_OPPONENT_INVENTORY", dataJson);
                String resp = networkService.sendRequest(req);

                if (resp != null && resp.contains("OK")) {
                    List<Card> cards = JsonUtils.parseOpponentInventory(resp);
                    for (Card c : cards) {
                        if (c.getId() == request.getInitiatorCardId()) {
                            Card foundCard = c;
                            Platform.runLater(() -> view.updateRemoteCardDisplay(foundCard));
                            return;
                        }
                    }
                }
            }
            Platform.runLater(() -> view.updateRemoteCardDisplay(null));
        }).start();
    }

    public void respondToTrade(TradeRequestModel request, boolean accepted) {
        System.out.println("Réponse échange : " + (accepted ? "OUI" : "NON"));

        int myId = localPlayer.getId_Client();
        String jsonData = JsonUtils.buildTradeResponseJson(accepted, request, myId);
        String requestStr = JsonUtils.buildResponse("ConfirmationResponse", jsonData);

        if (networkService != null) {
            networkService.sendRequest(requestStr);
        }

        notifications.remove(request);
        Platform.runLater(() -> primaryStage.setTitle("Robs Card Game"));
        showNotifications();
    }

    private String getUsernameById(int id) {
        String req = JsonUtils.buildRequest("GET_CONNECTED_USERS", "{}");
        String resp = networkService.sendRequest(req);
        if (resp != null) {
            List<Player> players = JsonUtils.parsePlayerList(resp);
            for(Player p : players) {
                if (p.getId_Client() == id) return p.getName();
            }
        }
        return null;
    }

    public void showNotifications() {
        primaryStage.setTitle("Robs Card Game");
        new NotificationView(primaryStage, this, notifications).show();
    }

    public void showLogin() {
        LoginView loginView = new LoginView(primaryStage);
        this.loginController = new LoginController(this, loginView);
        loginView.setController(this.loginController);
        loginView.show();
    }

    public void start() {
        int storedId = SessionService.loadClientId();

        if (storedId == 0) {
            showLogin();
        } else {
            System.out.println("ID trouvé : " + storedId + ". Reconnexion...");
            localPlayer.setId(storedId);

            String jsonData = JsonUtils.buildLoginData(storedId, null);
            String request = JsonUtils.buildRequest("LOGIN", jsonData);

            if (networkService != null) {
                String response = networkService.sendRequest(request);

                if (response != null && response.contains("OK")) {
                    List<Card> inventory = JsonUtils.parseInventoryFromLogin(response);
                    String username = JsonUtils.parseUsernameFromLogin(response); // Récupération du vrai nom

                    localPlayer.setName(username);
                    localPlayer.getInventory().clear();
                    localPlayer.getInventory().addAll(inventory);

                    System.out.println("Reconnexion OK. Joueur: " + username + ", " + inventory.size() + " cartes chargées.");
                    showMenu();
                } else {
                    System.err.println("Echec reconnexion. Retour au login.");
                    showLogin();
                }
            }
        }
    }

    public void showMenu() { menuView.show(); }

    public void showCombat() {
        this.combatController = new CombatController(this, localPlayer);
        CombatView combatView = new CombatView(primaryStage, this.combatController);
        combatView.show();
    }

    public void showInventory() {
        this.inventoryController = new InventoryController(this, localPlayer);
        InventoryView inventoryView = new InventoryView(primaryStage, this.inventoryController);
        inventoryView.show();
    }

    public void showCardCreation() { cardCreationView.show(); }

    public void showTrade() {
        TradeView tradeView = new TradeView(primaryStage);
        this.tradeController = new TradeController(this, localPlayer, tradeView);
        tradeView.setController(this.tradeController);
        tradeView.show();
        this.tradeController.refreshPlayerList();
    }

    public NetworkService getNetworkService() { return networkService; }

    public Player getLocalPlayer() { return this.localPlayer; }

    public void quit() {
        System.out.println("Déconnexion du client...");
        if (networkService != null) networkService.closeConnection();
        primaryStage.close();
    }
}
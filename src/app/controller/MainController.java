package app.controller;

import app.model.Card;
import app.model.Player;
import app.model.TradeRequestModel;
import app.service.JsonUtils;
import app.service.NetworkService;
import app.service.SessionService;
import app.views.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Cursor;

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
    private String pendingTradeOpponentName = "l'adversaire";

    private boolean isInCombat = false;

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

    private void showToast(String message, Runnable onClickAction) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            Label label = new Label(message);

            label.setWrapText(true);
            label.setMaxWidth(260);
            label.setPrefWidth(260);

            String bgColor = message.contains("refusé") ? "#F44336" : (message.contains("accepté") || message.contains("validé") ? "#4CAF50" : "#A97DDE");

            label.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: white; -fx-border-radius: 10; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0);");

            if (onClickAction != null) {
                label.setOnMouseEntered(e -> label.setCursor(Cursor.HAND));
                label.setOnMouseExited(e -> label.setCursor(Cursor.DEFAULT));

                label.setOnMouseClicked(e -> {
                    if (!isInCombat) {
                        onClickAction.run();
                        popup.hide();
                    }
                });
            } else {
                label.setOnMouseClicked(e -> popup.hide());
            }

            popup.getContent().add(label);
            popup.setAutoHide(true);

            if (primaryStage.isShowing()) {
                double x = primaryStage.getX() + primaryStage.getWidth() - 320;
                double y = primaryStage.getY() + 60;
                popup.show(primaryStage, x, y);
            }

            PauseTransition delay = new PauseTransition(Duration.seconds(4));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }

    private void handleNotification(String message) {
        System.out.println("Notification reçue : " + message);
        if (message.contains("FightConfirmationRequest")) {
            TradeRequestModel req = JsonUtils.parseFightRequestNotification(message); // Marque isFight=true

            new Thread(() -> {
                String name = getUsernameById(req.getInitiatorId());
                if (name != null) req.setInitiatorUsername(name);

                Platform.runLater(() -> {
                    notifications.add(req);
                    primaryStage.setTitle("Robs Card Game - (" + notifications.size() + ") Notification(s) !");

                    showToast("⚔️ DÉFI DE COMBAT de " + req.getInitiatorUsername() + " !", () -> {
                        new FightProposalView(primaryStage, this, req).show();
                    });
                });
            }).start();
        }

        if (message.contains("ConfirmationRequest")) {
            TradeRequestModel req = JsonUtils.parseTradeRequestNotification(message);

            new Thread(() -> {
                String name = getUsernameById(req.getInitiatorId());
                if (name != null) {
                    req.setInitiatorUsername(name);
                }

                Platform.runLater(() -> {
                    notifications.add(req);
                    primaryStage.setTitle("Robs Card Game - (" + notifications.size() + ") Notification(s) !");

                    showToast("Nouvelle demande d'échange de " + req.getInitiatorUsername() + " ! (Cliquez pour voir)", () -> {
                        new TradeProposalView(primaryStage, this, req).show();
                    });

                });
            }).start();
        }

        else if (message.contains("TradeResult")) {

            if (message.contains("ERROR")) {
                Platform.runLater(() -> {
                    showToast("Échange refusé par " + pendingTradeOpponentName + ".", null);
                });
            }

            else if (message.contains("OK")) {
                List<Card> newInventory = JsonUtils.parseInventoryFromTradeResult(message);

                if (!newInventory.isEmpty()) {
                    localPlayer.getInventory().clear();
                    localPlayer.getInventory().addAll(newInventory);
                    Platform.runLater(() -> {
                        showToast("Échange accepté par " + pendingTradeOpponentName + " ! Inventaire mis à jour.", null);
                    });
                }
            }
        }
    }

    public void respondToFight(TradeRequestModel request, boolean accepted, int myId) {
        System.out.println("Réponse Combat : " + (accepted ? "OUI" : "NON"));

        String jsonData = JsonUtils.buildFightResponseJson(accepted, request, myId);
        String responseStr = JsonUtils.buildResponse("Response FightRequest", jsonData);

        if (networkService != null) networkService.sendMessage(responseStr);

        notifications.remove(request);
        Platform.runLater(() -> primaryStage.setTitle("Robs Card Game"));
        showNotifications();
    }

    public void fetchRemoteCardForTrade(TradeRequestModel request, FightProposalView view) {
        new Thread(() -> {
            String opponentUsername = getUsernameById(request.getInitiatorId());
            if (opponentUsername == null) opponentUsername = getUsernameById(request.getInitiatorId());

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

    public void fetchRemoteCardForTrade(TradeRequestModel request, TradeProposalView view) {
        new Thread(() -> {
            String opponentUsername = getUsernameById(request.getInitiatorId());
            if (opponentUsername == null) opponentUsername = getUsernameById(request.getInitiatorId());

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

    public void respondToTrade(TradeRequestModel request, boolean accepted, int myId) {
        System.out.println("Réponse échange : " + (accepted ? "OUI" : "NON"));
        String jsonData = JsonUtils.buildTradeResponseJson(accepted, request, myId);
        String responseStr = JsonUtils.buildResponse("ConfirmationResponse", jsonData);

        if (networkService != null) networkService.sendMessage(responseStr);

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
        return "Joueur " + id;
    }

    public void showNotifications() {
        this.isInCombat = false;
        primaryStage.setTitle("Robs Card Game");
        new NotificationView(primaryStage, this, notifications).show();
    }

    public void showLogin() {
        this.isInCombat = false;
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
                    String username = JsonUtils.parseUsernameFromLogin(response);
                    localPlayer.setName(username);
                    localPlayer.getInventory().clear();
                    localPlayer.getInventory().addAll(inventory);
                    System.out.println("Reconnexion OK.");
                    showMenu();
                } else {
                    System.err.println("Echec reconnexion.");
                    showLogin();
                }
            }
        }
    }

    public void showMenu() {
        this.isInCombat = false;
        menuView.show();
    }

    public void showCombat() {
        this.isInCombat = true;
        this.combatController = new CombatController(this, localPlayer);
        CombatView combatView = new CombatView(primaryStage, this.combatController);
        combatView.show();
    }

    public void showInventory() {
        this.isInCombat = false;
        this.inventoryController = new InventoryController(this, localPlayer);
        InventoryView inventoryView = new InventoryView(primaryStage, this.inventoryController);
        inventoryView.show();
    }

    public void showCardCreation() {
        this.isInCombat = false;
        cardCreationView.show();
    }

    public void showTrade() {
        this.isInCombat = false;
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

    public void setPendingTradeOpponent(String name) {
        this.pendingTradeOpponentName = name;
    }
}
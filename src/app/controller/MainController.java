package app.controller;

import app.model.Card;
import app.model.FightResultModel;
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
    private int lastMyCardIdEngaged = -1; // Pour retrouver sa carte au résultat

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
            if (message.contains("COMBAT") || message.contains("terminé")) bgColor = "#FF5252";

            label.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: white; -fx-border-radius: 10; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 0);");

            if (onClickAction != null) {
                label.setOnMouseEntered(e -> label.setCursor(Cursor.HAND));
                label.setOnMouseExited(e -> label.setCursor(Cursor.DEFAULT));

                label.setOnMouseClicked(e -> {
                    onClickAction.run();
                    popup.hide();
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
            TradeRequestModel req = JsonUtils.parseFightRequestNotification(message);

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
        else if (message.contains("ConfirmationRequest")) {
            TradeRequestModel req = JsonUtils.parseTradeRequestNotification(message);

            new Thread(() -> {
                String name = getUsernameById(req.getInitiatorId());
                if (name != null) req.setInitiatorUsername(name);

                Platform.runLater(() -> {
                    notifications.add(req);
                    primaryStage.setTitle("Robs Card Game - (" + notifications.size() + ") Notification(s) !");
                    showToast("Nouvelle demande d'échange de " + req.getInitiatorUsername() + " !", () -> {
                        new TradeProposalView(primaryStage, this, req).show();
                    });
                });
            }).start();
        }
        else if (message.contains("FightResult")) {
            List<Card> newInventory = JsonUtils.parseInventoryFromTradeResult(message);
            localPlayer.getInventory().clear();
            localPlayer.getInventory().addAll(newInventory);

            FightResultModel result = JsonUtils.parseFightResult(message);

            Card myCardUpdated = null;
            if (this.lastMyCardIdEngaged != -1) {
                for(Card c : localPlayer.getInventory()) {
                    if (c.getId() == this.lastMyCardIdEngaged) {
                        myCardUpdated = c;
                        break;
                    }
                }
            }
            result.setMyCard(myCardUpdated);

            Platform.runLater(() -> {
                TradeRequestModel notif = new TradeRequestModel(0, 0, 0);
                notif.setFightResult(result);

                // --- CORRECTION : Utilisation du nom sauvegardé ---
                notif.setInitiatorUsername(this.pendingTradeOpponentName);
                // --------------------------------------------------

                notifications.add(notif);
                primaryStage.setTitle("Robs Card Game - (" + notifications.size() + ") Notification(s) !");

                showToast("Combat terminé ! Voir le résultat", () -> {
                    new FightResultView(primaryStage, this, result, result.getMyCard()).show();
                });
            });
        }

        else if (message.contains("TradeResult")) {
            if (message.contains("ERROR")) {
                Platform.runLater(() -> showToast("Action refusée par " + pendingTradeOpponentName + ".", null));
            }
            else if (message.contains("OK")) {
                List<Card> newInventory = JsonUtils.parseInventoryFromTradeResult(message);
                if (!newInventory.isEmpty()) {
                    localPlayer.getInventory().clear();
                    localPlayer.getInventory().addAll(newInventory);
                    Platform.runLater(() -> showToast("Action acceptée ! Inventaire mis à jour.", null));
                }
            }
        }
    }

    public void fetchRemoteCardForTrade(TradeRequestModel request, app.views.TradeProposalView view) {
        fetchRemoteCardGeneric(request, view::updateRemoteCardDisplay);
    }

    public void fetchRemoteCardForTrade(TradeRequestModel request, app.views.FightProposalView view) {
        fetchRemoteCardGeneric(request, view::updateRemoteCardDisplay);
    }

    private void fetchRemoteCardGeneric(TradeRequestModel request, java.util.function.Consumer<Card> callback) {
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
                            Platform.runLater(() -> callback.accept(foundCard));
                            return;
                        }
                    }
                }
            }
            Platform.runLater(() -> callback.accept(null));
        }).start();
    }

    public void respondToFight(TradeRequestModel request, boolean accepted, int myId) {
        System.out.println("Réponse Combat : " + (accepted ? "OUI" : "NON"));

        if (accepted) {
            this.setLastMyCardIdEngaged(request.getReceiverCardId());
            this.setPendingTradeOpponent(request.getInitiatorUsername());
        }

        String jsonData = JsonUtils.buildFightResponseJson(accepted, request, myId);
        String responseStr = JsonUtils.buildResponse("ResponseFightRequest", jsonData);

        if (networkService != null) networkService.sendMessage(responseStr);

        notifications.remove(request);
        Platform.runLater(() -> primaryStage.setTitle("Robs Card Game"));
        showNotifications();
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

    public void showLogin() { this.isInCombat = false; LoginView v = new LoginView(primaryStage); loginController = new LoginController(this, v); v.setController(loginController); v.show(); }
    public void start() {
        int storedId = SessionService.loadClientId();
        if (storedId == 0) showLogin();
        else {
            localPlayer.setId(storedId);
            String resp = networkService.sendRequest(JsonUtils.buildRequest("LOGIN", JsonUtils.buildLoginData(storedId, null)));
            if (resp != null && resp.contains("OK")) {
                localPlayer.setName(JsonUtils.parseUsernameFromLogin(resp));
                localPlayer.getInventory().clear();
                localPlayer.getInventory().addAll(JsonUtils.parseInventoryFromLogin(resp));
                showMenu();
            } else showLogin();
        }
    }
    public void showMenu() { this.isInCombat = false; menuView.show(); }
    public void showCombat() {
        this.isInCombat = true;
        combatController = new CombatController(this, localPlayer);
        new CombatView(primaryStage, combatController).show();
    }
    public void showInventory() { this.isInCombat = false; inventoryController = new InventoryController(this, localPlayer); new InventoryView(primaryStage, inventoryController).show(); }
    public void showCardCreation() { this.isInCombat = false; cardCreationView.show(); }
    public void showTrade() {
        this.isInCombat = false;
        TradeView t = new TradeView(primaryStage);
        this.tradeController = new TradeController(this, localPlayer, t);
        t.setController(tradeController);
        t.show();
        tradeController.refreshPlayerList();
    }
    public void quit() { if(networkService!=null) networkService.closeConnection(); primaryStage.close(); }
    public NetworkService getNetworkService() { return networkService; }
    public Player getLocalPlayer() { return localPlayer; }
    public void setPendingTradeOpponent(String name) { this.pendingTradeOpponentName = name; }
    public void setLastMyCardIdEngaged(int id) { this.lastMyCardIdEngaged = id; }
}
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MainController class serves as the central hub of the application.
 * It coordinates navigation between views, manages the local player's state,
 * and handles all incoming network notifications and responses.
 */
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
    private int lastMyCardIdEngaged = -1;

    /**
     * Initializes the MainController, sets up the network service, and defines the primary stage close behavior.
     * @param primaryStage The main window of the JavaFX application.
     */
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

    /**
     * Displays a temporary notification popup (toast) on the screen.
     * @param message The text content of the toast.
     * @param onClickAction Action to execute if the user clicks the toast.
     */
    private void showToast(String message, Runnable onClickAction) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            Label label = new Label(message);
            label.setWrapText(true);
            label.setMaxWidth(260);
            label.setPrefWidth(260);

            String bgColor = message.contains("refusé") ? "#F44336" : (message.contains("accepté") ? "#4CAF50" : "#A97DDE");
            label.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-padding: 15; -fx-background-radius: 10;");

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
            if (primaryStage.isShowing()) {
                popup.show(primaryStage, primaryStage.getX() + primaryStage.getWidth() - 320, primaryStage.getY() + 60);
            }

            PauseTransition delay = new PauseTransition(Duration.seconds(4));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }

    /**
     * Central listener for incoming network messages.
     * Processes fight challenges, trade requests, and results (trade or fight).
     * @param message The raw JSON string received from the server.
     */
    private void handleNotification(String message) {
        if (message.contains("FightConfirmationRequest")) {
            TradeRequestModel req = JsonUtils.parseFightRequestNotification(message);
            new Thread(() -> {
                String name = getUsernameById(req.getInitiatorId());
                if (name != null) req.setInitiatorUsername(name);
                Platform.runLater(() -> {
                    notifications.add(req);
                    showToast("Défi de combat de " + req.getInitiatorUsername(), () -> new FightProposalView(primaryStage, this, req).show());
                });
            }).start();
        } else if (message.contains("ConfirmationRequest")) {
            TradeRequestModel req = JsonUtils.parseTradeRequestNotification(message);
            new Thread(() -> {
                String name = getUsernameById(req.getInitiatorId());
                if (name != null) req.setInitiatorUsername(name);
                Platform.runLater(() -> {
                    notifications.add(req);
                    showToast("Demande d'échange de " + req.getInitiatorUsername(), () -> new TradeProposalView(primaryStage, this, req).show());
                });
            }).start();
        } else if (message.contains("TradeResult")) {
            if (message.contains("OK")) {
                List<Card> newInventory = JsonUtils.parseInventoryFromTradeResult(message);

                if (!newInventory.isEmpty()) {
                    localPlayer.getInventory().clear();
                    localPlayer.getInventory().addAll(newInventory);

                    Platform.runLater(() -> {
                        showToast("Échange réussi ! Inventaire mis à jour.", null);
                        if (primaryStage.getTitle().contains("Inventory")) {
                            showInventory();
                        }
                    });
                }
            } else if (message.contains("ERROR")) {
                Platform.runLater(() -> showToast("L'échange a été refusé.", null));
            }
        }else if (message.contains("FightResult")) {
            List<Card> newInventory = JsonUtils.parseInventoryFromTradeResult(message);
            localPlayer.getInventory().clear();
            localPlayer.getInventory().addAll(newInventory);

            FightResultModel result = JsonUtils.parseFightResult(message);

            if (message.contains("ERROR") || message.contains("REFUSED")) {
                result = new app.model.FightResultModel("L'adversaire a fui le combat", null);
            }

            int opponentId = -1;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"id_opponent\"\\s*:\\s*(\\d+)").matcher(message);
            if (m.find()) opponentId = Integer.parseInt(m.group(1));

            Card myCardUpdated = null;
            if (this.lastMyCardIdEngaged != -1) {
                for (Card c : localPlayer.getInventory()) {
                    if (c.getId() == this.lastMyCardIdEngaged) { myCardUpdated = c; break; }
                }
            }
            result.setMyCard(myCardUpdated);

            final int finalOpponentId = opponentId;
            final app.model.FightResultModel finalResult = result;

            new Thread(() -> {
                String opponentName = getUsernameById(finalOpponentId);

                Platform.runLater(() -> {
                    TradeRequestModel notif = new TradeRequestModel(finalOpponentId, 0, 0);
                    notif.setInitiatorUsername(opponentName);
                    notif.setFightResult(finalResult);
                    notifications.add(notif);

                    if (primaryStage.getTitle().contains("Notifications")) showNotifications();

                    showToast("Combat terminé contre " + opponentName, () -> {
                        new FightResultView(primaryStage, this, finalResult, finalResult.getMyCard()).show();
                    });
                });
            }).start();
        }
    }

    /**
     * Sends a response to a fight request to the server.
     * @param request The fight request model.
     * @param accepted Whether the fight is accepted.
     * @param myId The ID of the local player.
     */
    public void respondToFight(TradeRequestModel request, boolean accepted, int myId) {
        if (accepted) {
            this.setLastMyCardIdEngaged(request.getReceiverCardId());
            this.setPendingTradeOpponent(request.getInitiatorUsername());
        }
        String jsonData = JsonUtils.buildFightResponseJson(accepted, request, myId);
        String responseStr = JsonUtils.buildResponse("ResponseFightRequest", jsonData);
        if (networkService != null) networkService.sendMessage(responseStr);
        notifications.remove(request);
        Platform.runLater(this::showNotifications);
    }

    /**
     * Sends a response to a trade request to the server.
     * @param request The trade request model.
     * @param accepted Whether the trade is accepted.
     * @param myId The ID of the local player.
     */
    public void respondToTrade(TradeRequestModel request, boolean accepted, int myId) {
        String jsonData = JsonUtils.buildTradeResponseJson(accepted, request, myId);
        String responseStr = JsonUtils.buildResponse("ConfirmationResponse", jsonData);
        if (networkService != null) networkService.sendMessage(responseStr);
        notifications.remove(request);
        Platform.runLater(this::showNotifications);
    }

    /**
     * Fetches remote card data for display in the Trade Proposal view.
     * @param request The trade request.
     * @param view The view to update.
     */
    public void fetchRemoteCardForTrade(TradeRequestModel request, app.views.TradeProposalView view) {
        fetchRemoteCardGeneric(request, view::updateRemoteCardDisplay);
    }

    /**
     * Fetches remote card data for display in the Fight Proposal view.
     * @param request The fight request.
     * @param view The view to update.
     */
    public void fetchRemoteCardForTrade(TradeRequestModel request, app.views.FightProposalView view) {
        fetchRemoteCardGeneric(request, view::updateRemoteCardDisplay);
    }

    /**
     * Fetches remote card data for display in the Fight Proposal view.
     * @param request The fight request.
     * @param view The view to update.
     */
    private void fetchRemoteCardGeneric(TradeRequestModel request, java.util.function.Consumer<Card> callback) {
        new Thread(() -> {
            String opponentUsername = getUsernameById(request.getInitiatorId());
            if (opponentUsername != null) {
                String dataJson = JsonUtils.buildGetOpponentInventoryRequest(opponentUsername);
                String req = JsonUtils.buildRequest("GET_OPPONENT_INVENTORY", dataJson);
                String resp = networkService.sendRequest(req);
                if (resp != null && resp.contains("OK")) {
                    List<Card> cards = JsonUtils.parseOpponentInventory(resp);
                    for (Card c : cards) {
                        if (c.getId() == request.getInitiatorCardId()) {
                            Platform.runLater(() -> callback.accept(c));
                            return;
                        }
                    }
                }
            }
            Platform.runLater(() -> callback.accept(null));
        }).start();
    }

    /**
     * Displays the notification list view.
     */
    public void showNotifications() {
        this.isInCombat = false;
        new NotificationView(primaryStage, this, notifications).show();
    }

    /**
     * Requests the username of a connected client by their ID.
     * @param id The target client ID.
     * @return The username as a String.
     */
    private String getUsernameById(int id) {
        String resp = networkService.sendRequest(JsonUtils.buildRequest("GET_CONNECTED_USERS", "{}"));
        if (resp != null) {
            List<Player> players = JsonUtils.parsePlayerList(resp);
            for (Player p : players) {
                if (p.getId_Client() == id) return p.getName();
            }
        }
        return "Joueur " + id;
    }

    /**
     * Shows the login view.
     */
    public void showLogin() {
        LoginView v = new LoginView(primaryStage);
        loginController = new LoginController(this, v);
        v.setController(loginController);
        v.show();
    }

    /**
     * Main entry point of the controller logic.
     * Checks for an existing session or prompts for login.
     */
    public void start() {
        int storedId = SessionService.loadClientId();
        if (storedId == 0) {
            showLogin();
        } else {
            localPlayer.setId(storedId);
            String resp = networkService.sendRequest(JsonUtils.buildRequest("LOGIN", JsonUtils.buildLoginData(storedId, null))); //

            if (resp != null && resp.contains("OK")) {
                localPlayer.setName(JsonUtils.parseUsernameFromLogin(resp));
                localPlayer.getInventory().clear();
                localPlayer.getInventory().addAll(JsonUtils.parseInventoryFromLogin(resp));
                showMenu();
            } else {
                localPlayer.setId(0);
                localPlayer.getInventory().clear();
                showLogin();
            }
        }
    }

    /** Displays the main menu view. */
    public void showMenu() {
        menuView.show();
    }

    /** Displays the combat selection view. */
    public void showCombat() {
        combatController = new CombatController(this, localPlayer);
        new CombatView(primaryStage, combatController).show();
    }

    /** Displays the local player's inventory view. */
    public void showInventory() {
        inventoryController = new InventoryController(this, localPlayer);
        new InventoryView(primaryStage, inventoryController).show();
    }

    /** Displays the card creation view. */
    public void showCardCreation() {
        cardCreationView.show();
    }

    /** Displays the trading hub view. */
    public void showTrade() {
        TradeView t = new TradeView(primaryStage);
        this.tradeController = new TradeController(this, localPlayer, t);
        t.setController(tradeController);
        t.show();
        tradeController.refreshPlayerList();
    }

    /** Closes the active network connection and the UI window. */
    public void quit() {
        if (networkService != null) networkService.closeConnection();
        primaryStage.close();
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public void setPendingTradeOpponent(String name) {
        this.pendingTradeOpponentName = name;
    }

    public void setLastMyCardIdEngaged(int id) {
        this.lastMyCardIdEngaged = id;
    }

    /** Logs out the current user, clears the session, and exits. */
    public void logout() {
        app.service.SessionService.clearSession();
        quit();
    }
}
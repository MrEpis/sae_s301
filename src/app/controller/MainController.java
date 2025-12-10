package app.controller;

import app.service.JsonUtils;
import app.service.SessionService;
import javafx.stage.Stage;
import app.views.*;
import app.model.Player;
import app.service.NetworkService;

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

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.networkService = new NetworkService();
        this.localPlayer = new Player(0, "Inconnu");

        this.menuView = new MenuView(primaryStage, this);
        this.cardCreationView = new CardCreationView(primaryStage);

        this.cardCreationController = new CardCreationController(this, localPlayer, cardCreationView);
        this.cardCreationView.setController(this.cardCreationController);
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
                networkService.sendRequest(request);
                showMenu();
            }
        }
    }

    public void showMenu() {
        menuView.show();
    }

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

    public void showCardCreation() {
        cardCreationView.show();
    }

    public void showTrade() {
        TradeView tradeView = new TradeView(primaryStage);
        this.tradeController = new TradeController(this, localPlayer, tradeView);
        tradeView.setController(this.tradeController);
        tradeView.show();
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public Player getLocalPlayer() {
        return this.localPlayer;
    }

    public void quit() {
        System.out.println("Déconnexion du client...");

        if (networkService != null) {
            networkService.closeConnection();
        }
        primaryStage.close();
    }


}
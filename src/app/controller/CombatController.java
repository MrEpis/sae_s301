package app.controller;

import app.model.Card;
import app.model.Player;
import app.service.JsonUtils;
import app.service.NetworkService;
import app.views.CombatView;

import java.util.ArrayList;
import java.util.List;

public class CombatController {

    private final MainController mainController;
    private final Player currentPlayer;
    private CombatView combatView;

    // Liste pour mapper Nom -> ID
    private List<Player> connectedPlayers = new ArrayList<>();

    public CombatController(MainController mainController, Player player) {
        this.mainController = mainController;
        this.currentPlayer = player;
    }

    public void setView(CombatView view) {
        this.combatView = view;
    }

    public void backToMenu() {
        mainController.showMenu();
    }

    public List<Card> getLocalPlayerInventory() {
        return currentPlayer.getInventory();
    }

    // 1. Récupérer la liste des joueurs (identique à TradeController)
    public void refreshPlayerList() {
        System.out.println("Combat: Récupération liste joueurs...");
        String request = JsonUtils.buildRequest("GET_CONNECTED_USERS", "{}");

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            String response = net.sendRequest(request);
            if (response != null && response.contains("OK")) {
                List<Player> allPlayers = JsonUtils.parsePlayerList(response);

                this.connectedPlayers.clear();
                List<String> names = new ArrayList<>();

                for (Player p : allPlayers) {
                    if (p.getId_Client() != currentPlayer.getId_Client()) {
                        this.connectedPlayers.add(p);
                        names.add(p.getName());
                    }
                }
                combatView.updatePlayerList(names);
                combatView.displayStatus(connectedPlayers.size() + " adversaire(s) trouvé(s).");
            }
        }
    }

    // 2. Charger l'inventaire adverse pour choisir la cible
    public void loadOpponentInventory(String opponentName) {
        combatView.displayStatus("Espionnage de l'inventaire de " + opponentName + "...");
        String dataJson = JsonUtils.buildGetOpponentInventoryRequest(opponentName);
        String request = JsonUtils.buildRequest("GET_OPPONENT_INVENTORY", dataJson);

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            String response = net.sendRequest(request);
            if (response != null && response.contains("OK")) {
                List<Card> cards = JsonUtils.parseOpponentInventory(response);
                combatView.updateOpponentInventory(cards);
                combatView.displayStatus("Cible verrouillée : " + opponentName);
            }
        }
    }

    // 3. Envoyer la demande de combat
    public void sendFightRequest(String opponentName, int myCardId, int targetCardId) {
        // Retrouver l'ID de l'adversaire
        int opponentId = -1;
        for(Player p : connectedPlayers) {
            if(p.getName().equals(opponentName)) {
                opponentId = p.getId_Client();
                break;
            }
        }

        if (opponentId == -1) {
            combatView.displayStatus("Erreur : Adversaire introuvable.");
            return;
        }

        System.out.println("Envoi FightRequest à " + opponentName);
        combatView.displayStatus("Envoi du défi à " + opponentName + "...");

        // Construction de la requête
        String dataJson = JsonUtils.buildFightRequestData(
                currentPlayer.getId_Client(),
                myCardId,
                opponentId,
                targetCardId
        );

        String request = JsonUtils.buildRequest("FightRequest", dataJson);

        NetworkService net = mainController.getNetworkService();
        if (net != null) {
            // On utilise sendMessage (Async) ou sendRequest (Sync) selon votre choix
            // Ici sendRequest pour avoir l'ACK immédiat du serveur
            String response = net.sendRequest(request);

            if (response != null && response.contains("OK")) {
                combatView.displayStatus("Défi envoyé ! En attente de réponse...");
            } else {
                combatView.displayStatus("Erreur lors de l'envoi du défi.");
            }
        }
    }
}
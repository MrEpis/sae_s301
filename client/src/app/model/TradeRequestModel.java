package app.model;

/**
 * Represents a formal request between players for trading cards or initiating combat.
 * It also serves as a container for displaying combat results in the notification list.
 */
public class TradeRequestModel {
    private int initiatorId;
    private int initiatorCardId;
    private int receiverCardId;
    private String initiatorUsername;
    private boolean isFight;

    private FightResultModel fightResult;

    /**
     * Initializes a standard trade or fight request.
     * @param initiatorId The ID of the sender.
     * @param initiatorCardId The ID of the sender's card.
     * @param receiverCardId The ID of the receiver's card.
     */
    public TradeRequestModel(int initiatorId, int initiatorCardId, int receiverCardId) {
        this.initiatorId = initiatorId;
        this.initiatorCardId = initiatorCardId;
        this.receiverCardId = receiverCardId;
        this.initiatorUsername = "Joueur " + initiatorId;
        this.isFight = false;
    }

    public int getInitiatorId() {
        return initiatorId;
    }

    public int getInitiatorCardId() {
        return initiatorCardId;
    }

    public int getReceiverCardId() {
        return receiverCardId;
    }

    public String getInitiatorUsername() {
        return initiatorUsername;
    }

    public void setInitiatorUsername(String username) {
        this.initiatorUsername = username;
    }

    public boolean isFight() {
        return isFight;
    }

    public void setFight(boolean fight) {
        isFight = fight;
    }

    public void setFightResult(FightResultModel result) {
        this.fightResult = result;
    }

    public FightResultModel getFightResult() {
        return fightResult;
    }

    // Helper to check if the request contains a battle result
    public boolean isFightResult() {
        return fightResult != null;
    }

    /**
    * Provides a localized string representation for notifications.
     * @return A description of the request or result.
     */
    @Override
    public String toString() {
        if (isFightResult()) return "Résultat du combat";
        return (isFight ? "Défi de combat" : "Echange proposé") + " par " + initiatorUsername;
    }
}
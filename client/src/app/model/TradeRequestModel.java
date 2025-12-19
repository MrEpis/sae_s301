package app.model;

// Model used to handle trade or fight proposals between players
public class TradeRequestModel {
    private int initiatorId;
    private int initiatorCardId;
    private int receiverCardId;
    private String initiatorUsername;
    private boolean isFight;

    private FightResultModel fightResult;

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

    @Override
    public String toString() {
        if (isFightResult()) return "Résultat du combat";
        return (isFight ? "Défi de combat" : "Echange proposé") + " par " + initiatorUsername;
    }
}
package app.model;

public class TradeRequestModel {
    private int initiatorId;
    private int initiatorCardId;
    private int receiverCardId;
    private String initiatorUsername;

    public TradeRequestModel(int initiatorId, int initiatorCardId, int receiverCardId) {
        this.initiatorId = initiatorId;
        this.initiatorCardId = initiatorCardId;
        this.receiverCardId = receiverCardId;
        this.initiatorUsername = "Joueur " + initiatorId;
    }

    public int getInitiatorId() { return initiatorId; }
    public int getInitiatorCardId() { return initiatorCardId; }
    public int getReceiverCardId() { return receiverCardId; }

    public String getInitiatorUsername() { return initiatorUsername; }
    public void setInitiatorUsername(String username) { this.initiatorUsername = username; }

    @Override
    public String toString() {
        return "Echange proposé par " + initiatorUsername;
    }
}
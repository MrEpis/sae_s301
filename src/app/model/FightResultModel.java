package app.model;

public class FightResultModel {
    private String logMessage;
    private Card opponentCard;
    private Card myCard;

    public FightResultModel(String logMessage, Card opponentCard) {
        this.logMessage = logMessage;
        this.opponentCard = opponentCard;
    }

    public String getLogMessage() { return logMessage; }
    public Card getOpponentCard() { return opponentCard; }

    public Card getMyCard() { return myCard; }
    public void setMyCard(Card myCard) { this.myCard = myCard; }
}
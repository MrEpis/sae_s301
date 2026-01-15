package app.model;

/**
 * Data model representing the summary of a finished combat.
 * It stores the textual log of the battle and the final state of the involved cards.
 */
public class FightResultModel {
    private String logMessage;
    private Card opponentCard;
    private Card myCard;

    /**
     * Initializes a new fight result with a log and the opponent's card state.
     * @param logMessage The combat log.
     * @param opponentCard The opponent's card data.
     */
    public FightResultModel(String logMessage, Card opponentCard) {
        this.logMessage = logMessage;
        this.opponentCard = opponentCard;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public Card getOpponentCard() {
        return opponentCard;
    }

    public Card getMyCard() {
        return myCard;
    }

    public void setMyCard(Card myCard) {
        this.myCard = myCard;
    }
}
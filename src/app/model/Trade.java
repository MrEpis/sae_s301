package app.model;

public class Trade {
    private Player requestingPlayer;
    private Player respondingPlayer;
    private Card offeredCard;
    private Card requestedCard;
    private boolean accepted;

    public Trade(Player requestingPlayer, Player respondingPlayer, Card offeredCard, Card requestedCard) {
        this.requestingPlayer = requestingPlayer;
        this.respondingPlayer = respondingPlayer;
        this.offeredCard = offeredCard;
        this.requestedCard = requestedCard;
        this.accepted = false;
    }

    public Player getRequestingPlayer() {
        return requestingPlayer;
    }

    public Player getRespondingPlayer() {
        return respondingPlayer;
    }

    public Card getOfferedCard() {
        return offeredCard;
    }

    public Card getRequestedCard() {
        return requestedCard;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}

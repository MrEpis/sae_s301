package app.model;

/**
 * Represents a specific game instance or match between two players.
 * It manages the participants, the cards selected for the duel, and the winner.
 */
public class Game {
    private Player player1;
    private Player player2;
    private Card cardPlayer1;
    private Card cardPlayer2;
    private Player winner;

    /**
     * Creates a new match between two players.
     * @param player2 The second participant.
     * @param player1 The first participant.
     * @param cardPlayer1 The card for player 1.
     * @param cardPlayer2 The card for player 2.
     */
    public Game(Player player2, Player player1, Card cardPlayer1, Card cardPlayer2) {
        this.player2 = player2;
        this.player1 = player1;
        this.cardPlayer1 = cardPlayer1;
        this.cardPlayer2 = cardPlayer2;
        this.winner = null;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Card getCardPlayer1() {
        return cardPlayer1;
    }

    public Card getCardPlayer2() {
        return cardPlayer2;
    }

    public Player getWinner() {
        return winner;
    }
}
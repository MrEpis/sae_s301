package app.model;

// Represents a match session between two players
public class Game {
    private Player player1;
    private Player player2;
    private Card cardPlayer1;
    private Card cardPlayer2;
    private Player winner;

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
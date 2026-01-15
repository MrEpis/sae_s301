package app.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a user within the game system.
 * Each player has a unique network ID, a name, and a private collection of cards.
 */
public class Player {
    private int Id_Client;
    private String name;
    private List<Card> inventory;

    /**
     * Constructs a new player with an ID and a name.
     * @param id_Client The network client ID.
     * @param name The display name.
     */
    public Player(int id_Client, String name) {
        this.Id_Client = id_Client;
        this.name = name;
        this.inventory = new ArrayList<>();
    }

    public void setId(int id) {
        this.Id_Client = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId_Client() {
        return Id_Client;
    }

    public String getName() {
        return name;
    }

    public List<Card> getInventory() {
        return inventory;
    }

    public void addCard(Card card) {
        inventory.add(card);
    }
}
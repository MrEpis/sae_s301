package app.model;

/**
 * Represents a playable game card with specific combat attributes.
 * This class stores the card's unique identification, name, health points,
 * and combat statistics such as attack and defense.
 */
public class Card {
    private int id;
    private String nom;
    private int hp;
    private int def;
    private int atk;
    private String imagePath;

    /**
     * Constructs a new Card with the specified attributes.
     * @param id The unique card ID.
     * @param nom The card name.
     * @param hp Initial health points.
     * @param def Defense statistics.
     * @param atk Attack statistics.
     * @param imagePath Resource path for the card image.
     */
    public Card(int id, String nom, int hp, int def, int atk, String imagePath) {
        this.id = id;
        this.nom = nom;
        this.hp = hp;
        this.def = def;
        this.atk = atk;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public int getHp() {
        return hp;
    }

    public int getDef() {
        return def;
    }

    public int getAtk() {
        return atk;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
}
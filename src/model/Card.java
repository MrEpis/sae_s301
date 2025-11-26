package model;

public class Card {
    private String nom;
    private int hp;
    private int def;
    private int atk;

    public Card(String nom, int hp, int def, int atk){
        this.nom = nom;
        this.hp = hp;
        this.def = def;
        this.atk = atk;
    }
}
package app.model;

public class Card {
    private int id;
    private String nom;
    private int hp;
    private int def;
    private int atk;
    private String imagePath;

    public Card(int id, String nom, int hp, int def, int atk, String imagePath){
        this.id = id;
        this.nom = nom;
        this.hp = hp;
        this.def = def;
        this.atk = atk;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public int getHp() { return hp; }
    public int getDef() { return def; }
    public int getAtk() { return atk; }
    public String getImagePath() { return imagePath; }

    public void setHp(int hp) {
        this.hp = hp;
    }
}
package app.model;

public class Card {
    private String name;
    private int hp;
    private int def;
    private int atk;

    public Card(String name, int hp, int def, int atk){
        this.name = name;
        this.hp = hp;
        this.def = def;
        this.atk = atk;
    }

    public String getNom() {
        return name;
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

    public void setHp(int hp) {
        this.hp = hp;
    }
}
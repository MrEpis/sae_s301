package views;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GameView extends Application {

    private static final int LARGEUR_CARTE = 200;
    private static final int HAUTEUR_CARTE = 250;

    @Override
    public void start(Stage primaryStage) {
        // 1. Conteneur Principal : BorderPane
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #2e2e2e;");

        // 2. Zone du Joueur (Bas) : Main et Info
        VBox zoneJoueur = creerZoneMainJoueur();
        root.setBottom(zoneJoueur);
        BorderPane.setAlignment(zoneJoueur, Pos.CENTER);

        // 3. Champ de Bataille (Centre) : Duel Empilé
        // Le centre est un VBox qui empile le titre et les deux cartes actives
        VBox centreContainer = creerChampDeBatailleDuelEmpile();
        root.setCenter(centreContainer);

        // 4. Zone de l'Adversaire (Haut)
        HBox zoneAdversaire = creerZoneAdversaire();
        root.setTop(zoneAdversaire);

        // 5. Zones d'Information Latérales (Droite et Gauche)
        VBox infoDroite = creerZoneInformation("Actions", "Espace pour l'Énergie/Mana et le bouton 'Fin de tour'");
        root.setRight(infoDroite);
        BorderPane.setMargin(infoDroite, new Insets(10, 0, 10, 10));

        VBox infoGauche = creerZoneInformation("Statut/Decks", "Espace pour la Pioche et la Défausse");
        root.setLeft(infoGauche);
        BorderPane.setMargin(infoGauche, new Insets(10, 10, 10, 0));

        // 6. Configuration de la Scène et du Stage
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Squelette de Jeu de Cartes - Duel Empilé");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox creerChampDeBatailleDuelEmpile() {
        VBox duelBox = new VBox(20);
        duelBox.setPadding(new Insets(30));
        duelBox.setAlignment(Pos.CENTER);

        duelBox.setStyle("-fx-background-color: #4a4a4a; -fx-border-color: #c4c4c4; -fx-border-width: 3;");

        Label titreCentre = creerLabelStatut("CHAMP DE BATAILLE ", 20, "#e0e0e0");

        VBox slotAdversaire = creerSlotCarte("ADVERSAIRE", "#8b0000");

        Label vsLabel = creerLabelStatut("VS", 30, "#ffffff");
        vsLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 30));

        VBox slotJoueur = creerSlotCarte("JOUEUR", "#008b00");

        duelBox.getChildren().addAll(titreCentre, slotAdversaire, vsLabel, slotJoueur);
        return duelBox;
    }

    private VBox creerSlotCarte(String titre, String couleurBordure) {
        VBox slot = new VBox(5);
        slot.setPrefSize(LARGEUR_CARTE, HAUTEUR_CARTE);
        slot.setAlignment(Pos.CENTER);

        slot.setStyle(
                "-fx-background-color: #333333; " +
                        "-fx-border-color: " + couleurBordure + "; " +
                        "-fx-border-width: 4; " +
                        "-fx-border-style: solid;"
        );

        Label titreLabel = new Label(titre + " - Carte Active");
        titreLabel.setTextFill(Color.web("#ffffff"));
        titreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        slot.getChildren().add(titreLabel);
        return slot;
    }

    private VBox creerZoneMainJoueur() {
        VBox mainBox = new VBox(5);
        mainBox.setPadding(new Insets(10));
        mainBox.setAlignment(Pos.CENTER);

        Label titre = new Label("--- Main du Joueur (HBox pour les cartes) ---");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titre.setTextFill(Color.web("#ffffff"));

        HBox cartesMain = new HBox(15);
        cartesMain.setPrefHeight(150);
        cartesMain.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #555555; -fx-border-width: 2; -fx-border-radius: 5;");
        cartesMain.setAlignment(Pos.CENTER);

        Label placeholder = new Label("Espace pour les Cartes en Main");
        placeholder.setTextFill(Color.web("#888888"));
        placeholder.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        cartesMain.getChildren().add(placeholder);

        mainBox.getChildren().addAll(titre, cartesMain);
        return mainBox;
    }

    private HBox creerZoneAdversaire() {
        HBox zone = new HBox(50);
        zone.setPadding(new Insets(10));
        zone.setAlignment(Pos.CENTER);
        zone.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #8b0000; -fx-border-width: 2;");

        VBox statut = new VBox(5);
        statut.setAlignment(Pos.CENTER_LEFT);
        statut.getChildren().addAll(
                creerLabelStatut("Adversaire Name", 16, "#ffffff"),
                creerLabelStatut("PV : 20 / 20", 14, "#ff4d4d"),
                creerLabelStatut("Mana/Énergie : 0 / 1", 14, "#4d4dff")
        );

        HBox mainAdversaire = new HBox(5);
        mainAdversaire.setPrefWidth(200);
        mainAdversaire.setPrefHeight(80);
        mainAdversaire.setStyle("-fx-background-color: #0d0d0d; -fx-border-color: #555555; -fx-border-width: 1;");
        mainAdversaire.setAlignment(Pos.CENTER);
        mainAdversaire.getChildren().add(new Label("Main Adversaire (5 cartes)"));

        zone.getChildren().addAll(statut, mainAdversaire);
        return zone;
    }

    private VBox creerZoneInformation(String titre, String contenu) {
        VBox infoBox = new VBox(10);
        infoBox.setPrefWidth(180);
        infoBox.setAlignment(Pos.TOP_CENTER);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle("-fx-background-color: #383838; -fx-border-color: #696969; -fx-border-width: 1; -fx-border-radius: 5;");

        Label titreLabel = creerLabelStatut(titre, 16, "#ffffff");
        Label contenuLabel = creerLabelStatut(contenu, 12, "#aaaaaa");
        contenuLabel.setWrapText(true);

        infoBox.getChildren().addAll(titreLabel, contenuLabel);
        return infoBox;
    }

    private Label creerLabelStatut(String texte, int taille, String couleur) {
        Label label = new Label(texte);
        label.setFont(Font.font("Arial", taille));
        label.setTextFill(Color.web(couleur));
        return label;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
# SAE S3.01 - Jeu de Cartes - Groupe B

Ce projet implémente un jeu de cartes multijoueur avec une architecture client-serveur.
- **Serveur** : Codé en C, il gère la logique du jeu, la base de données (PostgreSQL) et la Blockchain.
- **Client** : Codé en Java avec JavaFX, il fournit l'interface graphique aux joueurs.

## Organisation des répertoires

L'architecture du projet sépare clairement les deux langages :

- `server/` : Contient tout le code source du serveur C.
  - `src/` : Fichiers sources (`.c`).
  - `include/` : Fichiers d'en-tête (`.h`).
  - `Makefile` : Script de compilation automatisé.
- `client/` : Contient tout le code source du client Java.
  - `src/` : Fichiers sources Java (`.java`) et ressources.
    - `app/` : Fichiers sources.
    - `ressources/` : Images pour les cartes.
  -  `test/` : Contient les tests unitaires du client.
  - `Makefile` : Script de compilation automatisé.
- `spe_technique.pdf` : Documentation technique du projet.

## Pré-requis

- **Système** : Linux
- **C** : `gcc`, `make`, `libpq-dev` (pour PostgreSQL)
- **Java** : JDK 21, JavaFX SDK 21
- **Base de données** : Connexion au réseau ou VPN de l'IUT afin d'avoir accès au serveur linserv

---

## Compilation et démarrage {#compilation}

### Serveur

Commandes à effectuer dans le dossier `server` :

Compilation : 
`make`

Démarrage du serveur :
`bin/server_exec`

Nettoyer les fichiers de sorties et l'exécutable : 
`make clean`

### Client

#### Pré-requis
Veuillez remplacer `<CHEMIN_VERS_JAVAFX_LIB>` dans les commandes ci-dessous par le chemin absolu vers le dossier `lib` de votre SDK JavaFX.
*(Exemple : `/usr/lib/jvm/javafx-sdk-21/lib` ou `/home/user/javafx-sdk-21/lib`)*

#### Configuration initiale de JavaFX
Commandes à effectuer dans le dossier `client`

`mkdir -p bin`
`find src -name "*.java" > sources.txt`
`javac -d bin \ -sourcepath src \ --module-path <CHEMIN_VERS_JAVAFX_LIB> \ --add-modules javafx.controls,javafx.fxml \ @sources.txt`

Optionnel : 
`rm sources.txt`

#### Compilation et exécution

Commandes à effectuer dans le dossier `client`

Pour seulement compiler :
`make`

Compiler et exécuter le client : 
`make run`

Pour spécifier l'adresse du serveur, ajouter cette option :
`SERVER_ADDR=<ADRESSE_IP_DU_SERVEUR>`

Pour lancer en mode bot : 
`make run-bot`

La session étant sauvegardée, il faut exécuter cette commande pour lancer une nouvelle session sur la même machine :
`make run-new`

Pour nettoyer les fichiers de sorties :
`make clean`

---

## Fonctionnement du jeu

### Première connexion {#premiere_connexion}

Lorsque un client se connecte pour la première fois au serveur, son nom d'utilisateur est demandé. Le joueur est sauvegardé par le serveur, mais aussi par le client : un fichier `session.dat` est créé et enregistre l'identifiant unique du joueur donné par le serveur. Lors de la prochaine connexion, l'utilisateur n'aura pas besoin de se reconnecter.


### Création de cartes {#creation_cartes}

Pour pouvoir jouer, l'utilisateur doit créer une carte. Il peut choisir le nom de sa carte ainsi que l'image à utiliser parmi une sélection d'une centaine d'images libres de droits (trouvées sur le site pixabay.com dans la catégorie "pixels"). Le nom et l'image de la carte n'ont aucune incidence sur le jeu et sont purement esthétiques. Le joueur peut également attribuer des stats à sa carte : il a au total 100 points à répartir entre les PV (Points de Vie), l'ATK (Attaque) et la DEF (Défense). Plus d'informations sur l'utilité de ces stats dans la section [Combat](#combat).
Une carte doit avoir au moins 1 PV mais peut avoir 0 en ATK et en DEF.
Une fois les attributs de la carte définis, le joueur peut l'enregistrer et pourra la voir dans son inventaire, l'utiliser en combat ou bien l'échanger. La carte est enregistrée dans la table `cartes` de la base de données et sa création est enregistrée dans la Blockchain.
Un joueur ne peut avoir plus de 5 cartes dans son inventaire. S'il essaye de créer une sixième carte, un message d'erreur s'affiche.

### Inventaire {#inventaire}

Dans son inventaire, le joueur peut voir les cartes qu'il possède, avec leur nom, image et stats actuelles.

### Combat {#combat}

Pour effectuer un combat avec un autre joueur connecté (ou bot), l'utilisateur choisit d'abord dans le menu déroulant un joueur cible. Il peut rafraîchir la liste ou sélectionner un adversaire. Ensuite, il peut choisir dans la liste de gauche une carte de son propre inventaire à utiliser dans le combat. Dans la liste de droite, il choisit la carte de l'adversaire à cibler. Une fois fait, il envoie la demande de combat à l'autre utilisateur. Celui-ci reçoit alors une notification sous la forme d'un pop-up cliquable. Il peut également la retrouver dans le menu "Notifications". Il peut alors accepter ou refuser le combat. Dans tous les cas, l'initiateur du combat reçoit à son tour une notification lui indiquant le résultat. Si l'adversaire a refusé le combat, aucune carte n'est affectée. Si le combat a été accepté, alors les deux cartes concernées s'attaquent : elles s'infligent des dégats en suivant la formule suivante : `DMG_TO_2 = ATK1 - (ATK1 * DEF2/100)`. 
Chaque combat n'est constitué que d'une seule attaque : les deux cartes peuvent très bien survivre au combat. Si les deux cartes surivivent ou meurent, le gagnant est la carte qui a infligé le plus de dégats. Si qu'une seule des deux cartes ne survit, alors le gagnant est la carte survivante. Lorsqu'une carte meurt, elle disparaît de l'inventaire du joueur. A l'issue du combat, les deux utilisateurs recoivent une notification pour afficher le résultat, qui indique clairement le gagnant et le perdant ainsi que le nouvel état des deux cartes.

### Échange {#echange}

Les utilisateurs peuvent aussi s'échanger des cartes. Le fonctionnement est le même que les combats : le joueur initiateur choisit un joueur dans la liste, choisit une carte de son inventaire et une carte de l'inventaire du receveur de l'offre. Après l'envoie de la proposition d'échange, le receveur reçoit une notification et peut accepter ou refuser l'échange. En cas de refus, rien ne change. Si l'échange est accepté, l'initiateur est averti et l'inventaire des deux joueurs est mis à jour.

### Bots

Il est possible de lancer le client en mode "Bot" (voir section [Compilation](#compilation)). Dans ce mode, le client se connecte avec le nom "roblobot" et effectue des opérations automatiquement. Lors de sa première connexion, il crée 4 cartes avec des stats déterminées aléatoirement. Lorsqu'un joueur réel demande un échange ou un combat au Bot, celui-ci les accepte systématiquement. Si le bot perd une carte suite à un combat, il en crée une nouvelle.

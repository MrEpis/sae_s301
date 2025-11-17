#include "server.h"
#include "blockchain.h"
#include "database.h"
#include <stdlib.h>
#include <stdio.h>

int main() {
    if (db_connect() != 0) {
        fprintf(stderr, "Echec de l'initialisation de la BDD. Arrêt.\n");
        return EXIT_FAILURE;
    }
    if (start_server() != 0) {
        perror("Le serveur n'a pas pu démarrer ou s'est arrêté avec une erreur\n");
        return EXIT_FAILURE;
    }
    db_close();
    return 0;
}
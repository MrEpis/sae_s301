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
    PGconn *conn = get_db_connection();

    int check = db_check_for_blockchain(conn);

    if (check == 1) {
        printf("Restauration du contexte depuis la BDD...\n");
        global_blockchain = db_load_blockchain(conn);
    } else if (check == 0) {
        printf("Aucune sauvegarde trouvée. Création d'une nouvelle Blockchain.\n");
        global_blockchain = create_new_blockchain();

        if (db_save_block(conn, global_blockchain->head) != 0) {
            perror("Echec de la sauvegarde du premier bloc.\n");
            return EXIT_FAILURE;
        }
    } else {
        fprintf(stderr, "Erreur critique de la BDD. Arrêt du serveur.\n");
        db_close();
        return EXIT_FAILURE;
    }
    
    if (global_blockchain == NULL) {
         fprintf(stderr, "Erreur critique: Blockchain non initialisée.\n");
         db_close();
         return EXIT_FAILURE;
    }

    if (start_server() != 0) {
        perror("Le serveur n'a pas pu démarrer ou s'est arrêté avec une erreur\n");
        return EXIT_FAILURE;
    }
    db_close();
    return 0;
}
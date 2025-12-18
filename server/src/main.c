#include "server.h"
#include "blockchain.h"
#include "database.h"
#include "structures.h"

#include <stdlib.h>
#include <stdio.h>

int main() {

    // Enregistrement du signal

    if (db_connect() != 0) {
        fprintf(stderr, "Echec de l'initialisation de la BDD. Arrêt.\n");
        return EXIT_FAILURE;
    }
    PGconn *conn = get_db_connection();

    int check = db_check_for_blockchain(conn);

    if (check == 1) {
        printf("Restauration du contexte depuis la BDD...\n");
        global_blockchain = db_load_blockchain(conn);

        if (!verify_blockchain_integrity(global_blockchain)) {
            fprintf(stderr, "[CRITIQUE] La blockchain est corrompue. Arrêt du serveur.\n");
            db_close();
            return EXIT_FAILURE;
        }
        if (!verify_consistency(conn, global_blockchain)) {
            fprintf(stderr, "[CRITIQUE] Incohérence détectée ! La BDD contient des cartes non validées par la blockchain. Arrêt du serveur.\n");
            db_close();
            return EXIT_FAILURE;
        }
         if (!verify_card_stats_integrity(conn, global_blockchain)) {
            fprintf(stderr, "[CRITIQUE] La base de données a été altérée manuellement (Stats modifiées).\n");
            db_close();
            return EXIT_FAILURE;
        }

    } else if (check == 0) {
        printf("[INFO] Aucune sauvegarde trouvée. Création d'une nouvelle Blockchain.\n");
        global_blockchain = create_new_blockchain();

        if (db_save_block(conn, global_blockchain->head) != 0) {
            perror("[ERREUR] Echec de la sauvegarde du premier bloc.\n");
            return EXIT_FAILURE;
        }
    } else {
        fprintf(stderr, "[CRITIQUE] Erreur critique de la BDD. Arrêt du serveur.\n");
        db_close();
        return EXIT_FAILURE;
    }
    
    if (global_blockchain == NULL) {
         fprintf(stderr, "[CRITIQUE] Blockchain non initialisée.\n");
         db_close();
         return EXIT_FAILURE;
    }

    if (start_server() != 0) {
        perror("[CRITIQUE] Le serveur n'a pas pu démarrer ou s'est arrêté avec une erreur\n");
        return EXIT_FAILURE;
    }


    printf("\n[INFO] Début du nettoyage des ressources...\n");

    // A. Libération de la Blockchain (Liste chaînée)
    if (global_blockchain != NULL) {
        Block *current = global_blockchain->head;
        while (current != NULL) {
            Block *next = current->next;
            
            // Si data_action a été alloué dynamiquement (strdup), il faut le libérer
            if (current->data_action != NULL) {
                free(current->data_action);
            }
            
            free(current); // Libération du bloc
            current = next;
        }
        free(global_blockchain); // Libération de la structure de contrôle
        printf("[INFO] Mémoire Blockchain libérée.\n");
    }

    // B. Fermeture de la Base de Données
    db_close(); 
    // Note: db_close appelle PQfinish(conn), donc pas besoin de le refaire ici 
    // si votre db_close utilise la variable statique interne, sinon passez 'conn'.

    printf("[INFO] Arrêt complet du programme. Au revoir !\n");
    return EXIT_SUCCESS;
}
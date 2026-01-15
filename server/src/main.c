#include "server.h"
#include "blockchain.h"
#include "database.h"
#include "structures.h"

#include <stdlib.h>
#include <stdio.h>


/**
 * Main function for the server.
 * Connects to database, verifies and gets the blockchain (or creates a new one) and starts the server.
 * When the server is closed, cleans up resources and closes database connection.
 */
int main() {

    if (db_connect() != 0) {
        fprintf(stderr, "Failed to connect to database. Stopping server.\n");
        return EXIT_FAILURE;
    }
    PGconn *conn = get_db_connection();

    int check = db_check_for_blockchain(conn);

    if (check == 1) {
        printf("Restoring blockchain from database...\n");
        global_blockchain = db_load_blockchain(conn);

        if (!verify_blockchain_integrity(global_blockchain)) {
            fprintf(stderr, "[CRITICAL] Blockchained is compromised. Stopping server.\n");
            db_close();
            return EXIT_FAILURE;
        }
        if (!verify_consistency(conn, global_blockchain)) {
            fprintf(stderr, "[CRITICAL] Inconsistency detected. Database contains cards not validated by blockchain. Stopping server.\n");
            db_close();
            return EXIT_FAILURE;
        }
         if (!verify_card_stats_integrity(conn, global_blockchain)) {
            fprintf(stderr, "[CRITICAL] Database was altered manually. Stopping server.\n");
            db_close();
            return EXIT_FAILURE;
        }

    } else if (check == 0) {
        printf("[INFO] No saved blockchain was found. Creating a new one...\n");
        global_blockchain = create_new_blockchain();

        if (db_save_block(conn, global_blockchain->head) != 0) {
            perror("[ERROR] Failed to save genesis block.\n");
            return EXIT_FAILURE;
        }
    } else {
        fprintf(stderr, "[CRITICAL] Critical database error. Stopping server.\n");
        db_close();
        return EXIT_FAILURE;
    }
    
    if (global_blockchain == NULL) {
         fprintf(stderr, "[CRITICAL] Blockchain is uninitialized.\n");
         db_close();
         return EXIT_FAILURE;
    }

    if (start_server() != 0) {
        perror("[CRITICAL] Server couldn't start or stopped with an error.\n");
        return EXIT_FAILURE;
    }


    printf("\n[INFO] Start of resources cleaning...\n");

    // Free the blockchain (linked list)
    if (global_blockchain != NULL) {
        Block *current = global_blockchain->head;
        while (current != NULL) {
            Block *next = current->next;

            if (current->data_action != NULL) {
                free(current->data_action);
            }
            
            free(current); // Free current block
            current = next;
        }
        free(global_blockchain); // Free the global control structure
        printf("[INFO] Blockchain memory successfully freed.\n");
    }

    // Close db connection
    db_close();

    printf("[INFO] Complete program shutdown. See you next time!\n");
    return EXIT_SUCCESS;
}
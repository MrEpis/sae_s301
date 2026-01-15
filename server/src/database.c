#include "structures.h"
#include "database.h"
#include "protocol.h"

#include <postgresql/libpq-fe.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define _POSIX_C_SOURCE 200809L

static PGconn *db_connection = NULL;

/**
 * Creates connection to database.
 */
int db_connect() {
    const char *conn_string = "host=linserv-info-01.campus.unice.fr port=5432 dbname=bl405485 user=bl405485 password=bl405485";

    printf("[INFO] Connecting to Database...\n");

    db_connection = PQconnectdb(conn_string);

    if (PQstatus(db_connection) != CONNECTION_OK) {
        fprintf(stderr, "[ERROR] Failed to connect to Database: %s\n", PQerrorMessage(db_connection));
        PQfinish(db_connection);
        db_connection = NULL;
        return -1;
    }

    printf("[INFO] Successfully connected to database.\n");
    return 0;
}

/**
 * Closes connection to databse.
 */
void db_close() {
    if (db_connection != NULL) {
        PQfinish(db_connection);
        db_connection = NULL;
        printf("[INFO] Connection to DB closed successfully.\n");
    }
}

/**
 * Simple function to get current connection to database.
 */
PGconn* get_db_connection() {
    return db_connection;
}

/**
 * Checks if blockchain is initialized in database.
 * Returns 1 if blockchain exists, 0 if not.
 */
int db_check_for_blockchain(PGconn *conn) {
    const char *query = "SELECT COUNT(*) FROM blockchain";
    PGresult *result = PQexec(conn, query);

    if (PQresultStatus(result) != PGRES_TUPLES_OK) {
        fprintf(stderr, "[ERROR] Checking for blockchain failed: %s\n", PQerrorMessage(conn));
        PQclear(result);
        return -1;
    }

    char *count_str = PQgetvalue(result, 0, 0);
    int count = atoi(count_str); //Conversion en int

    PQclear(result);

    printf("[INFO] Blockchain checking: %d blocks found.\n", count);

    if (count > 0) {
        return 1; // Blockchain exists
    } else {
        return 0; // Blockchain is empty
    }
}


/**
 * Loads blockchain from database
 */
Blockchain* db_load_blockchain(PGconn *conn) {
    const char *query = "SELECT * "
                        "FROM blockchain ORDER BY id_block ASC";
    
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "[ERROR] Failed to fetch blockchain from database: %s\n",
                PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    int rows = PQntuples(res);
    if (rows == 0) {
        printf("[ERROR] No blocks found in database.\n"); // Shouldn't ever occur
        PQclear(res);
        return NULL;
    }

    Blockchain *chain = malloc(sizeof(Blockchain));
    if (chain == NULL) {
        perror("[ERROR] Malloc Blockchain");
        PQclear(res);
        return NULL;
    }
    chain->head = NULL;
    chain->tail = NULL;
    chain->size = 0;

    printf("[INFO] Loading %d blocks from DB...\n", rows);

    for (int i = 0; i < rows; i++) {
        Block *new_block = malloc(sizeof(Block));
        if (new_block == NULL) {
            perror("[ERROR] Malloc Block");
            break;
        }

        new_block->ID_block = atoi(PQgetvalue(res, i, 0));
        new_block->timestamp = atol(PQgetvalue(res, i, 1));

        new_block->data_action = strdup(PQgetvalue(res, i, 2));

        strncpy(new_block->previous_hash, PQgetvalue(res, i, 3), HASH_SIZE);
        new_block->previous_hash[HASH_SIZE-1] = '\0';
        
        strncpy(new_block->hash, PQgetvalue(res, i, 4), HASH_SIZE);
        new_block->hash[HASH_SIZE - 1] = '\0';

        new_block->nonce = atoi(PQgetvalue(res, i, 5));

        new_block->next = NULL;

        // If first block (GENESIS)
        if (chain->head == NULL) {
            chain->head = new_block;
            chain->tail = new_block;
        } else {
            chain->tail->next = new_block;
            chain->tail = new_block;
        }

        chain->size++;
    }

    PQclear(res);

    printf("[INFO] Blockchain successfully restored. (Size: %d)\n", chain->size);
    return chain;
}

/**
 * Saves a mined block into the database.
 */
int db_save_block(PGconn *conn, Block *block) {
    const char *query = "INSERT INTO blockchain (id_block, timestamp, data_action, prev_hash, hash, nonce) "
                        "VALUES ($1, $2, $3, $4, $5, $6)";
    
    char id_str[12];
    char time_str[24];
    char nonce_str[12];

    sprintf(id_str, "%d", block->ID_block);
    sprintf(time_str, "%ld", block->timestamp);
    sprintf(nonce_str, "%d", block->nonce);

    const char *param_values[6];
    param_values[0] = id_str;
    param_values[1] = time_str;
    param_values[2] = block->data_action; // Le JSON brut (contient des "")
    param_values[3] = block->previous_hash;
    param_values[4] = block->hash;
    param_values[5] = nonce_str;

    PGresult *res = PQexecParams(conn, query, 6, NULL, param_values, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        fprintf(stderr, "[ERROR] Faild to insert block %d: %s\n",
                block->ID_block, PQerrorMessage(conn));
        PQclear(res);
        return -1;
    }

    PQclear(res);
    printf("[INFO] Block %d successfully saved in DB.\n", block->ID_block);
    return 0;
}

/**
 * Saves a newly created player in the database.
 */
int db_create_player(PGconn *conn, const char *username) {
    const char *query = "INSERT INTO joueurs (username) VALUES ($1) RETURNING id";
    const char *params[1] = { username };

    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "[ERROR] Faild to insert player in DB: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return -1;
    }

    int new_id = atoi(PQgetvalue(res, 0, 0));
    PQclear(res);

    printf("[INFO] New player created in DB: %s (ID: %d)\n", username, new_id);
    return new_id;
}


/**
 * Checks if a player exists in the database.
 * Used for basic verification when processing trades/fights.
 */
int db_player_exists(PGconn *conn, int id) {
    char id_str[12];
    sprintf(id_str, "%d", id);

    const char *query = "SELECT id FROM joueurs WHERE id = $1";
    const char *params[1] = { id_str };

    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    int exists = (PQntuples(res) > 0);
    PQclear(res);
    return exists;
}

/**
 * Gets a player ID from their username.
 * Used when searching for the player to trade/fight with since client only knows usernames.
 */
int db_get_player_id_by_name(PGconn *conn, const char *username) {
    const char *query = "SELECT id FROM joueurs WHERE username = $1";
    const char *params[1] = { username };

    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK || PQntuples(res) == 0) {
        PQclear(res);
        return -1;
    }

    int id = atoi(PQgetvalue(res, 0, 0));
    PQclear(res);
    return id;
}

/**
 * Gets a player's name from their ID.
 * Used when an existing player logs in since the client's own ID is stored.
 */
int db_get_username_by_id(PGconn *conn, int id, char *username_buffer) {
    char id_str[12];
    sprintf(id_str, "%d", id);
    const char *params[1] = { id_str };

    PGresult *res = PQexecParams(conn, "SELECT username FROM joueurs WHERE id = $1", 
                                 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK || PQntuples(res) == 0) {
        PQclear(res);
        return -1;
    }

    strcpy(username_buffer, PQgetvalue(res, 0, 0));
    
    PQclear(res);
    return 1;
}

/**
 * Gets a player's hand from the database.
 * Puts the hand in a JSON array to be sent to the client.
 */
void db_get_player_cards_json(PGconn *conn, int owner_id, char *buffer, int max_len) {
    char id_str[12];
    sprintf(id_str, "%d", owner_id);

    // Get all necessary stats and info to be displayed
    const char *query = "SELECT id, nom, attaque, defense, max_hp, hp_actuel, nom_image FROM cartes WHERE owner_id = $1";
    const char *params[1] = { id_str };

    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        snprintf(buffer, max_len, "[]"); // Erreur ou vide
        PQclear(res);
        return;
    }

    int rows = PQntuples(res);
    
    // Start of JSON array
    strcpy(buffer, "[");
    
    char temp[256];
    // For each card in player's hand
    for (int i = 0; i < rows; i++) {
        // Build card object
        snprintf(temp, sizeof(temp), 
                 "{\"id\": %s, \"nom\": \"%s\", \"attaque\": %s, \"defense\": %s, \"pv\": %s, \"image\": \"%s\"}",
                 PQgetvalue(res, i, 0), // id
                 PQgetvalue(res, i, 1), // nom
                 PQgetvalue(res, i, 2), // attaque
                 PQgetvalue(res, i, 3), // defense (si utilisé)
                 PQgetvalue(res, i, 5), // hp_actuel (on renvoie le HP courant comme PV)
                 PQgetvalue(res, i, 6)  // image_name
        );

        // Add to array
        strncat(buffer, temp, max_len - strlen(buffer) - 1);

        // Add coma if not last element
        if (i < rows - 1) {
            strncat(buffer, ", ", max_len - strlen(buffer) - 1);
        }
    }

    // End of JSON array
    strncat(buffer, "]", max_len - strlen(buffer) - 1);
    PQclear(res);
}

/**
 * Adds a newly created card in database.
 */
int db_create_card(PGconn *conn, int owner_id, const char *nom, int atk, int def, int hp, const char *img) {
    const char *query = "INSERT INTO cartes (owner_id, nom, attaque, defense, max_hp, hp_actuel, nom_image) "
                        "VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING id";
    
    char owner_str[12], atk_str[12], def_str[12], hp_str[12];
    sprintf(owner_str, "%d", owner_id);
    sprintf(atk_str, "%d", atk);
    sprintf(def_str, "%d", def);
    sprintf(hp_str, "%d", hp);

    const char *params[7] = { owner_str, nom, atk_str, def_str, hp_str, hp_str, img };

    PGresult *res = PQexecParams(conn, query, 7, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "[ERROR] Failed to insert card: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return -1;
    }

    int new_id = atoi(PQgetvalue(res, 0, 0));
    PQclear(res);
    return new_id;
}

/**
 * Checks the blockchain's integrity.
 * Compares the number of cards in the Blockchain with the number of cards stored in Database
 */
int verify_consistency(PGconn *conn, Blockchain *chain) {
    printf("--- Verifying consistency between Blockchain and database ---\n");
    
    // Get number of cards according to the blockchain
    int cards_in_blockchain = 0;
    Block *current = chain->head;
    
    while (current != NULL) {
        // Look for Card Creation blocks
        if (strstr(current->data_action, "\"action\": \"CreateCard\"") != NULL) {
            cards_in_blockchain++;
        }
        current = current->next;
    }

    // Get number of cards according to database
    PGresult *res = PQexec(conn, "SELECT COUNT(*) FROM cartes");
    int cards_in_db = atoi(PQgetvalue(res, 0, 0));
    PQclear(res);

    printf("[INFO] Cards in blockchain: %d\n", cards_in_blockchain);
    printf("[INFO] Cards in database: %d\n", cards_in_db);

    if (cards_in_blockchain < cards_in_db) {
        return 0;
    } else {
        printf("--- Consistency: OK ---\n");
        return 1;
    }
}

/**
 * Checks the integrity of card stats.
 * Compares the stats of cards stored in database with their stats as initialized in Blockchain.
 * If there is a discrepancy, one or mu:tiple cards were manually modified in database.
 */
int verify_card_stats_integrity(PGconn *conn, Blockchain *chain) {
    printf("--- Detailed verification of cards stats ---\n");
    
    Block *current = chain->head;
    //char buffer[1024];
    char id_str[20], atk_str[20], def_str[20], pv_str[20]; // Buffers pour extraction JSON
    
    int errors = 0;

    while (current != NULL) {
        // Search for Card creation blocks
        if (strstr(current->data_action, "\"action\": \"CreateCard\"") != NULL) {
            
            // Extract data from block
            extract_json_value(current->data_action, "card_id", id_str, sizeof(id_str));
            
            // Defaullt values if parsing somehow fails
            int bc_atk = 0, bc_def = 0, bc_hp = 0;
            
            if (extract_json_value(current->data_action, "attack", atk_str, sizeof(atk_str))) 
                bc_atk = atoi(atk_str);

            if (extract_json_value(current->data_action, "defense", def_str, sizeof(def_str))) 
                bc_def = atoi(def_str);
                
            if (extract_json_value(current->data_action, "max_hp", pv_str, sizeof(pv_str))) 
                bc_hp = atoi(pv_str);

            // Get data from database
            const char *query = "SELECT attaque, max_hp, defense FROM cartes WHERE id = $1";
            const char *params[1] = { id_str };
            
            PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

            if (PQresultStatus(res) == PGRES_TUPLES_OK && PQntuples(res) > 0) {
                int db_atk = atoi(PQgetvalue(res, 0, 0));
                int db_hp = atoi(PQgetvalue(res, 0, 1));
                int db_def = atoi(PQgetvalue(res, 0, 2));

                // Compare
                if (db_atk != bc_atk || db_hp != bc_hp || db_def != bc_def) {
                    fprintf(stderr, "[ALERT] Inconsistency for card ID %s !\n", id_str);
                    fprintf(stderr, "Blockchain: Atk=%d, Def=%d, HP=%d\n", bc_atk, bc_def, bc_hp);
                    fprintf(stderr, "Database: Atk=%d, Def=%d, HP=%d\n", db_atk, db_def, db_hp);
                    errors++;
                }
            } else {
                // Card not present in DB (deprecated since cards are deleted after death, so no errors are raised)
                fprintf(stderr, "[INFO] Card ID %s in Blockchain but missing from DB.\n", id_str);
            }
            PQclear(res);
        }
        current = current->next;
    }

    if (errors > 0) {
        printf("--- Check failed: %d inconsistencies found. ---\n", errors);
        return 0; 
    }
    
    printf("--- Card stats check OK ---\n");
    return 1;
}

/**
 * Trades two cards between two players in the database using a SQL transaction.
 */
int db_execute_trade(PGconn *conn, int id_init, int card_init, int id_recv, int card_recv) {
    PGresult *res;
    
    // Start transaction
    res = PQexec(conn, "BEGIN");
    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        PQclear(res);
        return -1;
    }
    PQclear(res);

    // Transfer initiator's card to receiver
    const char *q1 = "UPDATE cartes SET owner_id = $1 WHERE id = $2 AND owner_id = $3";
    char owner_recv_str[12], card_init_str[12], owner_init_str[12];
    sprintf(owner_recv_str, "%d", id_recv);
    sprintf(card_init_str, "%d", card_init);
    sprintf(owner_init_str, "%d", id_init);
    const char *p1[] = {owner_recv_str, card_init_str, owner_init_str};

    res = PQexecParams(conn, q1, 3, NULL, p1, NULL, NULL, 0);
    if (PQresultStatus(res) != PGRES_COMMAND_OK || atoi(PQcmdTuples(res)) == 0) {
        // If failure we can rollback
        PQclear(res);
        PQexec(conn, "ROLLBACK");
        return -1;
    }
    PQclear(res);

    // Transfer receiver's card to initiator
    const char *q2 = "UPDATE cartes SET owner_id = $1 WHERE id = $2 AND owner_id = $3";
    char card_recv_str[12];
    sprintf(card_recv_str, "%d", card_recv);
    const char *p2[] = {owner_init_str, card_recv_str, owner_recv_str}; // Notez l'inversion des owners

    res = PQexecParams(conn, q2, 3, NULL, p2, NULL, NULL, 0);
    if (PQresultStatus(res) != PGRES_COMMAND_OK || atoi(PQcmdTuples(res)) == 0) {
        // If failure we can rollback
        PQclear(res);
        PQexec(conn, "ROLLBACK");
        return -1;
    }
    PQclear(res);

    // Commit transaction
    res = PQexec(conn, "COMMIT");
    PQclear(res);
    
    return 0;
}

/**
 * Returns the amount of cards in a player's hand.
 * Used for card creation to check if a player has the max amount of cards.
 */
int db_count_player_cards(PGconn *conn, int id_client) {
    char id_str[12];
    sprintf(id_str, "%d", id_client);

    const char *query = "SELECT COUNT(*) FROM cartes WHERE owner_id = $1";
    const char *params[1] = { id_str };

    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);
    int client_cards = atoi(PQgetvalue(res, 0, 0));

    PQclear(res);
    return client_cards;
}

/**
 * Gets a card stats from db.
 * Used for fights.
 */
int db_get_card_stats(PGconn *conn, int card_id, int *atk, int *def, int *hp) {
    char id_str[12];
    sprintf(id_str, "%d", card_id);
    const char *params[1] = { id_str };

    PGresult *res = PQexecParams(conn, "SELECT attaque, defense, hp_actuel FROM cartes WHERE id = $1", 
                                 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK || PQntuples(res) == 0) {
        PQclear(res);
        return -1;
    }

    *atk = atoi(PQgetvalue(res, 0, 0));
    *def = atoi(PQgetvalue(res, 0, 1));
    *hp  = atoi(PQgetvalue(res, 0, 2));

    PQclear(res);
    return 0;
}

/**
 * Updates card hp in database after a fight.
 * If card is dead, it's deleted from the database.
 * Returns 0 if card is dead, 1 if card is still alive.
 */
int db_update_card_hp(PGconn *conn, int card_id, int new_hp) {
    char id_str[12], hp_str[12];
    sprintf(id_str, "%d", card_id);
    
    // If hp <= 0, delete card from database
    if (new_hp <= 0) {
        const char *params[1] = { id_str };
        PGresult *res = PQexecParams(conn, "DELETE FROM cartes WHERE id = $1", 
                                     1, NULL, params, NULL, NULL, 0);
        PQclear(res);
        return 0;
    } else {
        sprintf(hp_str, "%d", new_hp);
        const char *params[2] = { hp_str, id_str };
        PGresult *res = PQexecParams(conn, "UPDATE cartes SET hp_actuel = $1 WHERE id = $2", 
                                     2, NULL, params, NULL, NULL, 0);
        PQclear(res);
        return 1;
    }
}

/**
 * Returns JSON object for a single card.
 * Used to send fight results.
 */
void db_get_single_card_json(PGconn *conn, int card_id, char *buffer, int max_len) {
    char id_str[12];
    sprintf(id_str, "%d", card_id);
    const char *params[1] = { id_str };

    const char *query = "SELECT id, nom, attaque, defense, max_hp, hp_actuel, nom_image FROM cartes WHERE id = $1";
    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK || PQntuples(res) == 0) {
        snprintf(buffer, max_len, "{}"); // Objet vide si non trouvé
        PQclear(res);
        return;
    }

    snprintf(buffer, max_len, 
             "{\"id\": %s, \"nom\": \"%s\", \"attaque\": %s, \"defense\": %s, \"pv\": %s, \"image\": \"%s\"}",
             PQgetvalue(res, 0, 0),
             PQgetvalue(res, 0, 1),
             PQgetvalue(res, 0, 2),
             PQgetvalue(res, 0, 3),
             PQgetvalue(res, 0, 5), // hp_actuel
             PQgetvalue(res, 0, 6)
    );

    PQclear(res);
}

/**
 * Checks if a card exists in database.
 */
int db_card_exists(PGconn *conn, int card_id) {
    char id_str[12];
    snprintf(id_str, sizeof(id_str), "%d", card_id);
    const char *params[1] = { id_str };

    const char *query = "SELECT 1 FROM cartes WHERE id = $1";
    
    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "[ERREUR BDD] Echec verification existence carte %d : %s\n", 
                card_id, PQerrorMessage(conn));
        PQclear(res);
        return -1;
    }

    int exists = (PQntuples(res) > 0) ? 1 : 0;

    PQclear(res);
    return exists;
}
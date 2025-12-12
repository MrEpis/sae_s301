#include "structures.h"
#include "database.h"
#include "protocol.h"

#include <postgresql/libpq-fe.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define _POSIX_C_SOURCE 200809L

static PGconn *db_connection = NULL;

int db_connect() {
    const char *conn_string = "host=linserv-info-01.campus.unice.fr port=5432 dbname=bl405485 user=bl405485 password=bl405485";

    printf("Connexion à la base de donnée...\n");

    db_connection = PQconnectdb(conn_string);

    if (PQstatus(db_connection) != CONNECTION_OK) {
        fprintf(stderr, "La connexion à la BDD a échoué : %s\n", PQerrorMessage(db_connection));
        PQfinish(db_connection);
        db_connection = NULL;
        return -1;
    }

    printf("Connexion à la BDD réussie.\n");
    return 0;
}

void db_close() {
    if (db_connection != NULL) {
        PQfinish(db_connection);
        db_connection = NULL;
        printf("Connexion à la BDD fermée avec succès.\n");
    }
}

PGconn* get_db_connection() {
    return db_connection;
}

int db_check_for_blockchain(PGconn *conn) {
    //Requête pour compter les lignes
    const char *query = "SELECT COUNT(*) FROM blockchain";
    //Exécution de la requête
    PGresult *result = PQexec(conn, query);

    if (PQresultStatus(result) != PGRES_TUPLES_OK) {
        fprintf(stderr, "Erreur lors de la vérification de la blockchain : %s\n", PQerrorMessage(conn));
        PQclear(result);
        return -1;
    }

    //Récupération de la valeur (située en ligne 0, colonne 0)
    char *count_str = PQgetvalue(result, 0, 0);
    int count = atoi(count_str); //Conversion en int

    //Nettoyage de la mémoire du résultat
    PQclear(result);

    printf("Vérification de la BDD : %d blocs trouvés.\n", count);

    if (count > 0) {
        return 1; //La blockchain existe
    } else {
        return 0; //La blockchain est vide
    }
}

Blockchain* db_load_blockchain(PGconn *conn) {
    const char *query = "SELECT * "
                        "FROM blockchain ORDER BY id_block ASC";
    
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "Erreur lors du chargement de la blockchain : %s\n", 
                PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    int rows = PQntuples(res);
    if (rows == 0) {
        printf("Aucun bloc trouvé dans la BDD.\n");
        PQclear(res);
        return NULL;
    }

    Blockchain *chain = malloc(sizeof(Blockchain));
    if (chain == NULL) {
        perror("Erreur malloc Blockchain");
        PQclear(res);
        return NULL;
    }
    chain->head = NULL;
    chain->tail = NULL;
    chain->size = 0;

    printf("Chargement de %d blocks depuis la BDD...\n", rows);

    for (int i = 0; i < rows; i++) {
        Block *new_block = malloc(sizeof(Block));
        if (new_block == NULL) {
            perror("Erreur malloc Bloc");
            break;
        }

        new_block->ID_block = atoi(PQgetvalue(res, i, 0));
        new_block->timestamp = atol(PQgetvalue(res, i, 1));

        new_block->data_action = strdup(PQgetvalue(res, i, 2));

        strncpy(new_block->previous_hash, PQgetvalue(res, i, 3), HASH_SIZE);
        new_block->previous_hash[HASH_SIZE-1] = '\0'; //Sécurité
        
        strncpy(new_block->hash, PQgetvalue(res, i, 4), HASH_SIZE);
        new_block->hash[HASH_SIZE - 1] = '\0';

        new_block->nonce = atoi(PQgetvalue(res, i, 5));

        new_block->next = NULL;

        if (chain->head == NULL) { //Premier block (genesis) :
            chain->head = new_block;
            chain->tail = new_block;
        } else {
            chain->tail->next = new_block;
            chain->tail = new_block;
        }

        chain->size++;
    }

    PQclear(res);

    printf("Blockchain restaurée avec succès (Taille : %d)\n", chain->size);
    return chain;
}


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
        fprintf(stderr, "Erreur lors de l'insertion du bloc %d : %s\n", 
                block->ID_block, PQerrorMessage(conn));
        PQclear(res);
        return -1;
    }

    PQclear(res);
    printf("Block %d enregistré en BDD avec succès.\n", block->ID_block);
    return 0;
}

int db_create_player(PGconn *conn, const char *username) {
    const char *query = "INSERT INTO joueurs (username) VALUES ($1) RETURNING id";
    const char *params[1] = { username };

    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "Erreur de création joueur: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return -1;
    }

    int new_id = atoi(PQgetvalue(res, 0, 0));
    PQclear(res);

    printf("Nouveau joueur créé : %s (ID: %d)\n", username, new_id);
    return new_id;
}

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

int db_get_player_id_by_name(PGconn *conn, const char *username) {
    const char *query = "SELECT id FROM joueurs WHERE username = $1";
    const char *params[1] = { username };

    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK || PQntuples(res) == 0) {
        PQclear(res);
        return -1; // Introuvable
    }

    int id = atoi(PQgetvalue(res, 0, 0));
    PQclear(res);
    return id;
}

int db_get_username_by_id(PGconn *conn, int id, char *username_buffer) {
    char id_str[12];
    sprintf(id_str, "%d", id);
    const char *params[1] = { id_str };

    PGresult *res = PQexecParams(conn, "SELECT username FROM joueurs WHERE id = $1", 
                                 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK || PQntuples(res) == 0) {
        // ID introuvable ou erreur
        PQclear(res);
        return -1;
    }

    strcpy(username_buffer, PQgetvalue(res, 0, 0));
    
    PQclear(res);
    return 1;
}

void db_get_player_cards_json(PGconn *conn, int owner_id, char *buffer, int max_len) {
    char id_str[12];
    sprintf(id_str, "%d", owner_id);

    // On récupère toutes les infos nécessaires pour l'affichage client
    const char *query = "SELECT id, nom, attaque, defense, max_hp, hp_actuel, nom_image FROM cartes WHERE owner_id = $1";
    const char *params[1] = { id_str };

    PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        snprintf(buffer, max_len, "[]"); // Erreur ou vide
        PQclear(res);
        return;
    }

    int rows = PQntuples(res);
    
    // Début du tableau JSON
    strcpy(buffer, "[");
    
    char temp[256];
    for (int i = 0; i < rows; i++) {
        // Construction de l'objet carte
        // Attention à bien échapper les guillemets pour le JSON
        snprintf(temp, sizeof(temp), 
                 "{\"id\": %s, \"nom\": \"%s\", \"attaque\": %s, \"defense\": %s, \"pv\": %s, \"image\": \"%s\"}",
                 PQgetvalue(res, i, 0), // id
                 PQgetvalue(res, i, 1), // nom
                 PQgetvalue(res, i, 2), // attaque
                 PQgetvalue(res, i, 3), // defense (si utilisé)
                 PQgetvalue(res, i, 5), // hp_actuel (on renvoie le HP courant comme PV)
                 PQgetvalue(res, i, 6)  // image_name
        );

        // Ajouter au buffer principal
        strncat(buffer, temp, max_len - strlen(buffer) - 1);

        // Ajouter une virgule si ce n'est pas le dernier élément
        if (i < rows - 1) {
            strncat(buffer, ", ", max_len - strlen(buffer) - 1);
        }
    }

    // Fin du tableau JSON
    strncat(buffer, "]", max_len - strlen(buffer) - 1);
    PQclear(res);
}

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
        fprintf(stderr, "Erreur insert carte: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return -1;
    }

    int new_id = atoi(PQgetvalue(res, 0, 0));
    PQclear(res);
    return new_id;
}

int verify_consistency(PGconn *conn, Blockchain *chain) {
    printf("--- Vérification de la cohérence BDD vs Blockchain ---\n");
    
    // 1. Récupérer le nombre de cartes officiel selon la Blockchain
    int cards_in_blockchain = 0;
    Block *current = chain->head;
    
    while (current != NULL) {
        // On cherche les actions de création
        if (strstr(current->data_action, "\"action\": \"CreateCard\"") != NULL) {
            cards_in_blockchain++;
        }
        current = current->next;
    }

    // 2. Récupérer le nombre de cartes dans la table SQL
    PGresult *res = PQexec(conn, "SELECT COUNT(*) FROM cartes");
    int cards_in_db = atoi(PQgetvalue(res, 0, 0));
    PQclear(res);

    printf("Cartes dans la Blockchain : %d\n", cards_in_blockchain);
    printf("Cartes dans la table SQL  : %d\n", cards_in_db);

    if (cards_in_blockchain != cards_in_db) {
        return 0;
    } else {
        printf("--- Cohérence : OK ---\n");
        return 1;
    }
}

int verify_card_stats_integrity(PGconn *conn, Blockchain *chain) {
    printf("--- Vérification détaillée des stats des cartes ---\n");
    
    Block *current = chain->head;
    //char buffer[1024];
    char id_str[20], atk_str[20], def_str[20], pv_str[20]; // Buffers pour extraction JSON
    
    int errors = 0;

    while (current != NULL) {
        // On ne s'intéresse qu'aux blocs de création
        if (strstr(current->data_action, "\"action\": \"CreateCard\"") != NULL) {
            
            // 1. Extraction des données "Vérité Blockchain"
            // Note : Adaptez les clés selon votre format JSON exact ("attaque", "pv", etc.)
            extract_json_value(current->data_action, "card_id", id_str, sizeof(id_str));
            
            // Valeurs par défaut si parsing échoue (sécurité)
            int bc_atk = 0, bc_def = 0, bc_hp = 0;
            
            if (extract_json_value(current->data_action, "attack", atk_str, sizeof(atk_str))) 
                bc_atk = atoi(atk_str);

            if (extract_json_value(current->data_action, "defense", def_str, sizeof(def_str))) 
                bc_def = atoi(def_str);
                
            if (extract_json_value(current->data_action, "max_hp", pv_str, sizeof(pv_str))) 
                bc_hp = atoi(pv_str); // On suppose PV = MaxHP à la création

            // 2. Récupération des données "État BDD"
            const char *query = "SELECT attaque, max_hp, defense FROM cartes WHERE id = $1";
            const char *params[1] = { id_str };
            
            PGresult *res = PQexecParams(conn, query, 1, NULL, params, NULL, NULL, 0);

            if (PQresultStatus(res) == PGRES_TUPLES_OK && PQntuples(res) > 0) {
                int db_atk = atoi(PQgetvalue(res, 0, 0));
                int db_hp = atoi(PQgetvalue(res, 0, 1));
                int db_def = atoi(PQgetvalue(res, 0, 2));

                // 3. Comparaison
                if (db_atk != bc_atk || db_hp != bc_hp || db_def != bc_def) {
                    fprintf(stderr, "[ALERTE TRICHE] Incohérence Carte ID %s !\n", id_str);
                    fprintf(stderr, "Blockchain : Atk=%d, Def=%d, HP=%d\n", bc_atk, bc_def, bc_hp);
                    fprintf(stderr, "Base de Données : Atk=%d, Def=%d, HP=%d\n", db_atk, db_def, db_hp);
                    errors++;
                }
            } else {
                // La carte est dans la blockchain mais pas dans la BDD (Suppression illégale ?)
                fprintf(stderr, "[ALERTE] Carte ID %s présente dans la Blockchain mais absente de la BDD.\n", id_str);
                errors++;
            }
            PQclear(res);
        }
        current = current->next;
    }

    if (errors > 0) {
        printf("--- ECHEC Vérification : %d incohérences trouvées ---\n", errors);
        return 0; 
    }
    
    printf("--- Stats des cartes intègres ---\n");
    return 1;
}
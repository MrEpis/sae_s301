#include "structures.h"
#include "database.h"

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

    printf("Blockchain restaurées avec succès (Taille : %d)\n", chain->size);
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
#include <postgresql/libpq-fe.h>
#include <stdio.h>
#include <stdlib.h>

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
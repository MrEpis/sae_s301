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
#ifndef DATABASE_H
#define DATABASE_H

#include <postgresql/libpq-fe.h>
#include "structures.h"

int db_connect();

void db_close();

PGconn* get_db_connection();

int db_check_for_blockchain(PGconn *conn);

Blockchain* db_load_blockchain(PGconn *conn);

int db_save_block(PGconn *conn, Block *block);

int db_create_player(PGconn *conn, const char *username);

int db_player_exists(PGconn *conn, int id);

int db_get_player_id_by_name(PGconn *conn, const char *username);

void db_get_player_cards_json(PGconn *conn, int owner_id, char *buffer, int max_len);

#endif
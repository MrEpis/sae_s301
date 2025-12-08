#ifndef DATABASE_H
#define DATABASE_H

#include <postgresql/libpq-fe.h>

int db_connect();

void db_close();

PGconn* get_db_connection();

int db_check_for_blockchain(PGconn *conn);

Blockchain* db_load_blockchain(PGconn *conn);

int db_save_block(PGconn *conn, Block *block);

#endif
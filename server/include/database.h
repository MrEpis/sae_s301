#ifndef DATABASE_H
#define DATABASE_H

#include <postgresql/libpq-fe.h>

int db_connect();

void db_close();

PGconn* get_db_connection();

#endif
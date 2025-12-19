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

int db_create_card(PGconn *conn, int owner_id, const char *nom, int atk, int def, int hp, const char *img);

int db_get_username_by_id(PGconn *conn, int id, char *username_buffer);

int verify_consistency(PGconn *conn, Blockchain *chain);

int verify_card_stats_integrity(PGconn *conn, Blockchain *chain);

int db_execute_trade(PGconn *conn, int id_init, int card_init, int id_recv, int card_recv);

int db_count_player_cards(PGconn *conn, int id_client);

int db_get_card_stats(PGconn *conn, int card_id, int *atk, int *def, int *hp);

int db_update_card_hp(PGconn *conn, int card_id, int new_hp);

void db_get_single_card_json(PGconn *conn, int card_id, char *buffer, int max_len);

int db_card_exists(PGconn *conn, int card_id);

#endif
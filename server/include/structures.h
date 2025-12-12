#ifndef STRUCTURES_H
#define STRUCTURES_H

#include <time.h>
#include <pthread.h>

#define HASH_SIZE 65
#define DIFFICULTY 4
#define MAX_CLIENTS 1

typedef struct Client {
    int client_id;
    char username[50];
    int socket;
    int logged_in;
} ConnectedPlayer;

extern ConnectedPlayer clients_list[MAX_CLIENTS];
extern pthread_mutex_t clients_mutex;
extern pthread_mutex_t db_mutex;
extern pthread_mutex_t bc_mutex;

typedef struct Card {
    int card_id;
    int owner_id;
    char name[50];
    int attack;
    int defense;
    int max_hp;
    int current_hp;
    char file_name[50];
} Card;

typedef struct Block {
    int ID_block;
    long timestamp;
    char* data_action;
    char previous_hash[HASH_SIZE];
    int nonce;
    char hash[HASH_SIZE];
    struct Block *next;
} Block;

typedef struct Blockchain {
    struct Block *head;
    struct Block *tail;
    int size;
} Blockchain;

#endif
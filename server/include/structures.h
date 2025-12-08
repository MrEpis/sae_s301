#ifndef STRUCTURES_H
#define STRUCTURES_H

#include <time.h>

#define HASH_SIZE 65
#define DIFFICULTY 4


typedef struct Client {
    int client_id;
    char username[50];
} Client;

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
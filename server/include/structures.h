#ifndef STRUCTURES_H
#define STRUCTURES_H

#include <time.h>

#define HASH_SIZE 65
#define DIFFICULTY 4



struct Client;

struct Player;

struct Card;

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
#include "blockchain.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/sha.h>

void calculate_hash(const char* input, char* output_hash) {
    unsigned char hash_bytes[SHA256_DIGEST_LENGTH];
    SHA256((unsigned char*)input, strlen(input), hash_bytes);

    for(int i=0; i<SHA256_DIGEST_LENGTH; i++) {
        sprintf(output_hash + (i * 2), "%02x", hash_bytes[i]);
    }
    output_hash[HASH_SIZE - 1] = '\0';
}

static char* block_to_string_for_hashing(Block* block) {
    //Estimation simple d'allocation mémoire, à modifier
    int len = snprintf(NULL, 0, "%d%ld%s%s%d",
                         block->ID_block,
                        block->timestamp,
                        block->data_action,
                        block->previous_hash,
                        block->nonce);
    
    char* str = malloc(len + 1);
    if (!str) {
        perror("malloc block_to_string");
        return NULL;
    }

    sprintf(str, "%d%ld%s%s%d", 
            block->ID_block, 
            block->timestamp, 
            block->data_action, 
            block->previous_hash, 
            block->nonce);

    return str;
}

static void mine_block(Block *block) {
    char hash_check[DIFFICULTY + 1];

    char target[DIFFICULTY + 1];
    memset(target, '0', DIFFICULTY);
    target[DIFFICULTY] = '\0';

    printf("Minage du bloc %d...\n", block->ID_block);

    block->nonce = 0;
    while (1) {
        char* block_data = block_to_string_for_hashing(block);
        if (!block_data) return; //Erreur de malloc

        calculate_hash(block_data, block->hash);
        free(block_data);

        strncpy(hash_check, block->hash, DIFFICULTY);
        hash_check[DIFFICULTY] = '\0';

        if (strcmp(hash_check, target) == 0) {
            printf("Block miné. Hash: %s\n", block->hash);
            break; //Hash valide trouvé
        }

        block->nonce++;
    }
}

Blockchain* create_new_blockchain() {
    Blockchain* chain = malloc(sizeof(Blockchain));
    if (!chain) {
        perror("malloc chain");
        return NULL;
    }
    chain->size = 1;

    Block* genesis = malloc(sizeof(Block));
    if (!genesis) {
        perror("malloc genesis");
        free(chain);
        return NULL;
    }

    genesis->ID_block = 0;
    genesis->timestamp = time(NULL);
    genesis->data_action = strdup("Genesis Block");
    genesis->nonce = 0;
    genesis->next = NULL;
    memset(genesis->previous_hash, '0', HASH_SIZE-1);
    genesis->previous_hash[HASH_SIZE-1] = '\0';

    mine_block(genesis);

    chain->head = genesis;
    chain->tail = genesis;

    printf("Blockchain créée avec le bloc Genesis.\n");
    return chain;
}
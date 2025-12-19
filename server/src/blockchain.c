#include "blockchain.h"
#include "structures.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/sha.h>
#include <time.h>

/**
 * Computes the SHA256 hash of a given input string.
 * output_hash must be at least HASH_SIZE (65 bytes) long.
 */
void calculate_hash(const char* input, char* output_hash) {
    unsigned char hash_bytes[SHA256_DIGEST_LENGTH];
    SHA256((unsigned char*)input, strlen(input), hash_bytes);

    for(int i=0; i<SHA256_DIGEST_LENGTH; i++) {
        sprintf(output_hash + (i * 2), "%02x", hash_bytes[i]);
    }
    output_hash[HASH_SIZE - 1] = '\0';
}

/**
 * Concatenates block data into a single string for hashing.
 * Returns a dynamically allocated string (must be freed by the caller).
 */
char* block_to_string_for_hashing(Block* block) {
    if (block == NULL) return NULL;

    // Calculate required size for the string
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

    // Write data to the string
    sprintf(str, "%d%ld%s%s%d", 
            block->ID_block, 
            block->timestamp, 
            block->data_action, 
            block->previous_hash, 
            block->nonce);

    return str;
}


/**
 * Performs Proof of Work (PoW).
 * Increments the nonce until the block's hash starts with the target difficulty.
 */
void mine_block(Block *block) {
    char hash_check[DIFFICULTY + 1];

    // Difficulty (default is 4 zeros)
    char target[DIFFICULTY + 1];
    memset(target, '0', DIFFICULTY);
    target[DIFFICULTY] = '\0';

    printf("[INFO] Mining block %d...\n", block->ID_block);

    block->nonce = 0;
    while (1) {
        char* block_data = block_to_string_for_hashing(block);
        if (!block_data) return; // Safety check

        calculate_hash(block_data, block->hash);
        free(block_data); // Free immediately to avoid memory leaks inside the loop

        // Check if the hash starts with the correct amount of zeros
        strncpy(hash_check, block->hash, DIFFICULTY);
        hash_check[DIFFICULTY] = '\0';

        if (strcmp(hash_check, target) == 0) {
            printf("[INFO] Mined block. Hash: %s\n", block->hash);
            break; // Valid hash found
        }

        block->nonce++;
    }
}

/**
 * Initializes a new blockchain with a Genesis block.
 */
Blockchain* create_new_blockchain() {
    Blockchain* chain = malloc(sizeof(Blockchain));
    if (!chain) {
        perror("[ERROR] Malloc blockchain\n");
        return NULL;
    }
    chain->size = 1;

    Block* genesis = malloc(sizeof(Block));
    if (!genesis) {
        perror("[ERROR] Malloc genesis block\n");
        free(chain);
        return NULL;
    }

    // Configure Genesis block
    char temp_json[256];
    snprintf(temp_json, sizeof(temp_json),
             "{\"action\": \"GENESIS\", \"info_hash\": \"Chain initialization\", \"difficulty\": %d}", 
             DIFFICULTY);
    genesis->ID_block = 0;
    genesis->timestamp = time(NULL);
    genesis->data_action = strdup(temp_json);
    genesis->nonce = 0;
    genesis->next = NULL;
    memset(genesis->previous_hash, '0', HASH_SIZE-1); // No previous hash
    genesis->previous_hash[HASH_SIZE-1] = '\0';

    mine_block(genesis); // Mine to get a valid hash

    chain->head = genesis;
    chain->tail = genesis;

    printf("[INFO] Blockchain created with Genesis block.\n");
    return chain;
}

/**
 * Verifies the complete integrity of the blockchain (chain linking + hash validity).
 * Returns 1 if valid, 0 otherwise.
 */
int verify_blockchain_integrity(Blockchain *chain) {
    if (chain == NULL || chain->head == NULL) return 1; // Empty chain is considered valid

    Block *current = chain->head;
    Block *previous = NULL;
    char calculated_hash[HASH_SIZE];
    char *data_string;

    printf("[CHECK] Verifying blockchain integrity\n");

    while (current != NULL) {
        // 1. Verify chaining (ignore genesis which has no previous hash)
        if (previous != NULL) {
            if (strcmp(current->previous_hash, previous->hash) != 0) {
                fprintf(stderr, "[ERROR] Chain break at block %d!\n", current->ID_block);
                fprintf(stderr, "   Expected: %s\n   Received: %s\n", previous->hash, current->previous_hash);
                return 0; // Blockchain is invalid
            }
        }

        // 2. Verify content by recalculating each block's hash
        data_string = block_to_string_for_hashing(current);
        if (!data_string) return 0;

        calculate_hash(data_string, calculated_hash);
        free(data_string);

        if (strcmp(calculated_hash, current->hash) != 0) {
            fprintf(stderr, "[ERROR] Data tampering detected at block %d!\n", current->ID_block);
            fprintf(stderr, "   DB Hash      : %s\n   Calc Hash    : %s\n", current->hash, calculated_hash);
            return 0; // Blockchain is invalid
        }

        previous = current;
        current = current->next;
    }

    printf("[CHECK] Blockchain integrity verified.\n");
    return 1; // Blockchain is valid
}
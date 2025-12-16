#include "blockchain.h"
#include "structures.h"
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

/* Crée la chaine de données d'un bloc pour le hachage */
char* block_to_string_for_hashing(Block* block) {
    //Estimation simple d'allocation mémoire, à modifier
    int len = snprintf(NULL, 0, "%d%ld%s%s%d",
                        block->ID_block,
                        block->timestamp,
                        block->data_action,
                        block->previous_hash,
                        block->nonce);
    
    //On malloc la chaine de retour
    char* str = malloc(len + 1);
    if (!str) {
        perror("malloc block_to_string");
        return NULL;
    }

    //On écrit les infos du bloc dans la chaine
    sprintf(str, "%d%ld%s%s%d", 
            block->ID_block, 
            block->timestamp, 
            block->data_action, 
            block->previous_hash, 
            block->nonce);

    return str;
}

void mine_block(Block *block) {
    char hash_check[DIFFICULTY + 1];

    //On crée d'abord une chaine de zéros
    char target[DIFFICULTY + 1];
    memset(target, '0', DIFFICULTY);
    target[DIFFICULTY] = '\0';

    printf("[INFO] Minage du bloc %d...\n", block->ID_block);

    block->nonce = 0;
    while (1) {
        char* block_data = block_to_string_for_hashing(block);
        if (!block_data) return; //Erreur de malloc

        calculate_hash(block_data, block->hash);
        free(block_data);

        //On vérifie si le hash commence par le bon nombre (DIFFICULTY) de zéros
        strncpy(hash_check, block->hash, DIFFICULTY);
        hash_check[DIFFICULTY] = '\0';

        if (strcmp(hash_check, target) == 0) {
            printf("[INFO] Block miné. Hash: %s\n", block->hash);
            break; //Hash valide trouvé
        }

        block->nonce++;
    }
}

Blockchain* create_new_blockchain() {
    Blockchain* chain = malloc(sizeof(Blockchain));
    if (!chain) {
        perror("[ERREUR] Malloc blockchain\n");
        return NULL;
    }
    chain->size = 1;

    Block* genesis = malloc(sizeof(Block));
    if (!genesis) {
        perror("[ERREUR] Malloc bloc genesis\n");
        free(chain);
        return NULL;
    }

    char temp_json[256];
    snprintf(temp_json, sizeof(temp_json),
             "{\"action\": \"GENESIS\", \"info_hash\": \"Initialisation de la chaine\", \"difficulty\": %d}", 
             DIFFICULTY);
    genesis->ID_block = 0;
    genesis->timestamp = time(NULL);
    genesis->data_action = strdup(temp_json);
    genesis->nonce = 0;
    genesis->next = NULL;
    memset(genesis->previous_hash, '0', HASH_SIZE-1);
    genesis->previous_hash[HASH_SIZE-1] = '\0';

    mine_block(genesis);

    chain->head = genesis;
    chain->tail = genesis;

    printf("[INFO] Blockchain créée avec le bloc Genesis.\n");
    return chain;
}

int verify_blockchain_integrity(Blockchain *chain) {
    if (chain == NULL || chain->head == NULL) return 1; // Vide = valide

    Block *current = chain->head;
    Block *previous = NULL;
    char calculated_hash[HASH_SIZE];
    char *data_string;

    printf("--- Démarrage de la vérification d'intégrité de la Blockchain ---\n");

    while (current != NULL) {
        // 1. Vérification du chaînage (sauf pour le Genesis)
        if (previous != NULL) {
            if (strcmp(current->previous_hash, previous->hash) != 0) {
                fprintf(stderr, "[ERREUR] Rupture de chaîne au bloc %d !\n", current->ID_block);
                fprintf(stderr, "Attendu : %s\nReçu    : %s\n", previous->hash, current->previous_hash);
                return 0; // Invalide
            }
        }

        // 2. Vérification du contenu (Tamper check)
        // On recrée la string utilisée pour le hashage
        data_string = block_to_string_for_hashing(current);
        if (!data_string) return 0;

        // On recalcule le hash
        calculate_hash(data_string, calculated_hash);
        free(data_string);

        // On compare avec le hash stocké
        if (strcmp(calculated_hash, current->hash) != 0) {
            fprintf(stderr, "[ERREUR] Contenu modifié au bloc %d !\n", current->ID_block);
            fprintf(stderr, "Hash BDD    : %s\nHash Calculé: %s\n", current->hash, calculated_hash);
            return 0; // Invalide
        }

        // Avancer
        previous = current;
        current = current->next;
    }

    printf("--- Intégrité Blockchain : OK ---\n");
    return 1; // Valide
}
#include <stdio.h>
#include <assert.h>
#include <string.h>

#include "protocol.h"
#include "blockchain.h"

void test_json_extraction() {
    char *json = "{\"type\":\"request\", \"data\": {\"id\": 123}}";
    char output[50];
    
    // Test d'extraction simple
    int res = extract_json_value(json, "type", output, 50);
    assert(res == 1);
    assert(strcmp(output, "request") == 0);
    
    printf("[TEST] JSON Parser : OK\n");
}

void test_blockchain_hashing() {
    // Créer un bloc factice
    Block b;
    b.ID_block = 0;
    b.nonce = 0;
    strcpy(b.previous_hash, "0000");
    b.data_action = "test";
    
    // Vérifier que le hash est généré
    char hash[65];
    calculate_hash(block_to_string_for_hashing(&b), hash);
    assert(strlen(hash) == 64);
    
    printf("[TEST] Blockchain Hashing : OK\n");
}

int main() {
    test_json_extraction();
    test_blockchain_hashing();
    printf("--- TOUS LES TESTS UNITAIRES ONT REUSSI ---\n");
    return 0;
}
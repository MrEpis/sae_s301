#ifndef BLOCKCHAIN_H
#define BLOCKCHAIN_H

#include "structures.h"

Blockchain* create_new_blockchain();

void calculate_hash(const char* input, char* output_hash);

char* block_to_string_for_hashing(Block* block);

void mine_block(Block *block);

#endif
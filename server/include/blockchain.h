#ifndef BLOCKCHAIN_H
#define BLOCKCHAIN_H

#include "structures.h"

Blockchain* create_new_blockchain();

void add_block();

char calculate_hash(const char* input, char* output_hash);

#endif
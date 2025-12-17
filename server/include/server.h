#ifndef SERVER_H
#define SERVER_H

#include "structures.h"

#define PORT 8080
#define MAX_PENDING_CONNECTIONS 5

extern Blockchain *global_blockchain;

Blockchain* get_global_blockchain();

int start_server();

void handle_sigint(int sig);

#endif
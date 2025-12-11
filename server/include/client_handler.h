#ifndef CLIENT_HANDLER_H
#define CLIENT_HANDLER_H

void *handle_client(void *arg);

void process_login(int client_socket, const char *json_payload);

void process_card_creation(int client_socket, const char *json_payload);

#endif
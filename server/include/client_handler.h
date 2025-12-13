#ifndef CLIENT_HANDLER_H
#define CLIENT_HANDLER_H

void *handle_client(void *arg);

void process_login(int client_socket, const char *json_payload);

void process_card_creation(int client_socket, const char *json_payload);

void process_get_connected_users(int client_socket);

void process_get_opponent_inventory(int client_socket, const char *json_payload);

void process_trade_request(int client_socket, const char *json_payload);

void process_trade_response(int client_socket, const char *json_payload);

#endif
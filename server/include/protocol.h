#ifndef PROTOCOL_H
#define PROTOCOL_H


#define ACTION_LOGIN "LOGIN"
#define ACTION_CREATION "RequestCardCreation"
#define ACTION_TRADE "TradeRequest"
#define ACTION_FIGHT "FightRequest"

int extract_json_value(const char *json, const char *key, char *output, int max_len);

void send_error_response(int socket, const char *action, const char *message);

#endif
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/socket.h>

#include "protocol.h"


int extract_json_value(const char *json, const char *key, char *output, int max_len) {
    char search_key[50];
    snprintf(search_key, sizeof(search_key), "\"%s\"", key);

    char *pos = strstr(json, search_key);
    if (!pos) return 0; 

    pos += strlen(search_key);
    pos = strchr(pos, ':');
    if (!pos) return 0;
    pos++; 

    while (*pos && isspace(*pos)) pos++;

    const char *start = pos;
    const char *end = NULL;

    
    if (*start == '"') {
        // Cas 1 : C'est une chaîne de caractères
        start++; 
        end = strchr(start, '"'); 
    } else if (*start == '{') {
        // Cas 2 : C'est un objet JSON (ex: le champ "data")
        // Il faut compter les accolades pour trouver la fin
        int brace_count = 1;
        const char *p = start + 1;
        while (*p && brace_count > 0) {
            if (*p == '{') brace_count++;
            if (*p == '}') brace_count--;
            p++;
        }
        if (brace_count == 0) {
            end = p; // p pointe juste après le '}' final
            // On recule de 1 pour ne pas inclure le caractère après le '}'
            // (mais strncpy prendra la longueur, donc 'end' sert de borne)
        }
    } else {
        // Cas 3 : Nombre ou Booléen
        end = start;
        while (*end && *end != ',' && *end != '}' && *end != ']' && !isspace(*end)) {
            end++;
        }
    }

    if (!end) return 0;

    int length = end - start;
    if (length >= max_len) length = max_len - 1;

    strncpy(output, start, length);
    output[length] = '\0';

    return 1;
}

void send_error_response(int socket, const char *action, const char *message) {
    char response[512];
    // Construction manuelle du JSON
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"ERROR\", \"data\": \"%s\"}\n", 
             action, message);
    
    printf("[SERVEUR -> CLIENT] (Socket client %d) [ERREUR] %s\n", socket, response);
    send(socket, response, strlen(response), 0);
}
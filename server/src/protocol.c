#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "protocol.h"


int extract_json_value(const char *json, const char *key, char *output, int max_len) {
    char search_key[50];

    // Préparation de la clé avec des guillemets pour éviter les faux positifs
    snprintf(search_key, sizeof(search_key), "\"%s\"", key);

    char *pos = strstr(json, search_key);
    if (!pos) return 0; // Clé non trouvée

    // Avancer après la clé
    pos += strlen(search_key);

    // Recherche du ':'
    pos = strchr(pos, ':');
    if (!pos) return 0;
    pos++;

    // On ignore les espaces
    while (*pos && isspace(*pos)) pos++;

    // Début de la valeur
    const char *start = pos;
    const char *end;

    // Si la valeur est entre guillemets (String)
    if (*start == '"') {
        start++; // Pour passer le premier guillemet
        end = strchr(start, '"'); // La fin est le guillemet fermant
    } else {
        // Si c'est un nombre ou un booléen (on s'arrête à la virgule ou accolade)
        end = strpbrk(start, ",}");
    }

    if (!end) return 0; // Format invalide

    // Calculer la longueur à copier
    int length = end - start;
    if (length >= max_len) length = max_len - 1;

    // Copier la valeur
    strncpy(output, start, length);
    output[length] = '\0'; // Terminer la chaine

    return 1;

}

void send_error_response(int socket, const char *action, const char *message) {
    char response[512];
    // Construction manuelle du JSON
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"ERROR\", \"data\": \"%s\"}\n", 
             action, message);
    
    send(socket, response, strlen(response), 0);
}
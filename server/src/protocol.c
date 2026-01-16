#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/socket.h>

#include "protocol.h"

/**
 * Custom JSON parser.
 * Extracts the data elements associated with the key passed as function parameter from a JSON object.
 * Example: when a user logs in, use "username" key to get their username.
 */
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
        // Case 1 : it's a string
        start++; 
        end = strchr(start, '"'); 
    } else if (*start == '{') {
        // Case 2 : it's a JSON object (for example "data" field)
        // Count the braces to find the end
        int brace_count = 1;
        const char *p = start + 1;
        while (*p && brace_count > 0) {
            if (*p == '{') brace_count++;
            if (*p == '}') brace_count--;
            p++;
        }
        if (brace_count == 0) {
            end = p; // points to character after final '}'
            // Move back 1 to not include that chararacter
            // (strncpy takes length so end will serve as a bound)
        }
    } else {
        // Case 3 : number or boolean
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
/**
 * Builds and sends an error message for the action type passed as parameters.
 */
void send_error_response(int socket, const char *action, const char *message) {
    char response[512];
    // Construction manuelle du JSON
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"ERROR\", \"data\": \"%s\"}\n", 
             action, message);
    
    printf("[SERVER -> CLIENT] (client socket %d) [ERROR] %s\n", socket, response);
    send(socket, response, strlen(response), 0);
}
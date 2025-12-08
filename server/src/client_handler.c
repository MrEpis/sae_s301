#include "client_handler.h"
#include "protocol.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/socket.h>

void *handle_client(void *arg) {
    //On récupère le socket du client passé en argument
    int client_socket = *(int*)arg;
    free(arg); 
    
    char buffer[2048]; // Taille suffisante pour un JSON
    char action_name[50];
    ssize_t bytes_read;


    //Boucle de lecture simple en mode bloquant
    bytes_read = recv(client_socket, buffer, sizeof(buffer)-1, 0);
    while (bytes_read > 0) { //Si on a bien reçu le message
        buffer[bytes_read] = '\0';
        printf("Message reçu du client %d: %s\n", client_socket, buffer);

        if (extract_json_value(buffer, "nom", action_name, sizeof(action_name))) {

            if (strcmp(action_name, ACTION_LOGIN) == 0) {
                char id_str[10];
                if (extract_json_value(buffer, "data", id_str, sizeof(id_str))) {
                    int id = atoi(id_str);
                    printf("Action LOGIN détectée pour ID %d\n", id);

                    // TODO: process_login(client_socket, id);

                    // Réponse temporaire de test
                    char response[200];
                    sprintf(response, "{\"type\": \"response\", \"nom\": \"LOGIN\", \"status\": \"OK\", \"data\": %d}\n", id);
                    send(client_socket, response, strlen(response), 0);
                }
            }
            else if (strcmp(action_name, ACTION_CREATION) == 0) {
                printf("Action CREATION détectée\n");
                // TODO: process_creation(client_socket, buffer);
            }
            else {
                send_error_response(client_socket, action_name, "Action inconnue");
            }
        } else {
            send_error_response(client_socket, "UNKNOWN", "Format JSON invalide");
        }
    }

    printf("Fermeture du socket client %d\n", client_socket);
    close(client_socket);
    pthread_exit(NULL);
}
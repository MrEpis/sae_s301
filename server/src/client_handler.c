#include "client_handler.h"

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
    
    char buffer[1024] = {0};
    ssize_t bytes_read;


    //Boucle de lecture simple en mode bloquant
    bytes_read = recv(client_socket, buffer, sizeof(buffer)-1, 0);
    
    if (bytes_read > 0) { //Si on a bien reçu le message
        buffer[bytes_read] = '\0';
        printf("Message reçu du client %d: %s\n", client_socket, buffer);

        if (strstr(buffer, "\"LOGIN\"") != NULL) {
            /* A FAIRE : Extraire l'ID proprement
            Ici, on simule juste une réponse positive pour l'ID 1. */
            //On construit la réponse JSON selon le protocole
            char *response = "{\"type\": \"response\", \"nom\": \"LOGIN\", \"data\": 1, \"status\": \"OK\"}\n";

            send(client_socket, response, strlen(response), 0);
            printf("Réponse envoyée : %s", response);
        } else {
            //Gestion de l'erreur si on a pas un LOGIN
            char *error_response = "{\"status\": \"ERROR\", \"message\": \"Requete inconnue\"}\n";
            send (client_socket, error_response, strlen(error_response), 0);
        }

    } else if (bytes_read == 0) { //Si le client s'est déconnecté
        printf("Client %d déconnecté.\n", client_socket);
    } else { //En cas d'erreur
        perror("recv failed");
    }

    printf("Fermeture du socket client %d\n", client_socket);
    close(client_socket);
    pthread_exit(NULL);
}
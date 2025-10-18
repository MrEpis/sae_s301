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

    const char *msg = "Connexion réussie, bienvenue sur le serveur !\n";
    send(client_socket, msg, strlen(msg), 0);

    //Boucle de lecture simple en mode bloquant
    bytes_read = recv(client_socket, buffer, 1024, 0);
    if (bytes_read > 0) { //Si on a bien reçu le message
        printf("Message reçu du client %d: %s\n", client_socket, buffer);
    } else if (bytes_read == 0) { //Si le client s'est déconnecté
        printf("Client %d déconnecté.\n", client_socket);
    } else { //En cas d'erreur
        perror("recv");
    }

    printf("Fermeture du socket client %d\n", client_socket);
    close(client_socket);
    pthread_exit(NULL);
}
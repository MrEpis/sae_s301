#include "server.h"
#include "client_handler.h"
#include "structures.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>

ConnectedPlayer clients_list[MAX_CLIENTS];
pthread_mutex_t clients_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t db_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t bc_mutex = PTHREAD_MUTEX_INITIALIZER;

Blockchain *global_blockchain = NULL;

Blockchain* get_global_blockchain() {
    return global_blockchain;
}

int start_server() {
    int server_fd, new_socket;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);

    // Initialiser la liste des clients
    for (int i = 0; i < MAX_CLIENTS; i++) {
        clients_list[i].client_id = -1;
        clients_list[i].socket = -1;
        clients_list[i].logged_in = 0;
    }

    //On crée le socket du serveur
    //AF_INET = IPv4, SOCK_STREAM = TCP
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("socket failed");
        return -1;
    }

    //Fonction pour rendre le socket réutilisable après un arrêt
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) {
        perror("setsockopt");
        return -1;
    }

    //On configure l'adresse et le port
    address.sin_family = AF_INET; //AF_INET = IPv4
    address.sin_addr.s_addr = INADDR_ANY; //Ecoute sur toutes les interfaces
    //htons() convertit change l'ordre de l'octet en entrée pour correspondre à un octet réseau (avec le bit de poids fort placé en premier)
    address.sin_port = htons(PORT); 

    //On lie le socket au port (bind)
    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address)) < 0) {
        perror("bind failed");
        close(server_fd);
        return -1;
    }

    //On met le port en mode écoute
    if (listen(server_fd, MAX_PENDING_CONNECTIONS) < 0) {
        perror("listen");
        close(server_fd);
        return -1;
    }

    //Boucle d'acceptation des clients
    while (1) {
        //La fonction accept() est bloquante et attend une connexion
        new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t*)&addrlen);
        if (new_socket < 0) {
            perror("accept");
            continue; //Ici on continue juste à écouter
        }

        printf("Nouvelle connexion acceptée, socket client : %d\n", new_socket);

        //On instancie un nouveau thread pour ce client
        pthread_t client_thread;
        
        //On alloue de la mémoire pour passer le socket au thread
        //car new_socket sera écrasé à la prochaine boucle
        int *client_socket_ptr = malloc(sizeof(int));
        if (client_socket_ptr == NULL) {
            perror("malloc pour thread");
            close(new_socket);
            continue;
        }
        *client_socket_ptr = new_socket;

        //On crée le thread en sur la fonction handle_client()
        if (pthread_create(&client_thread, NULL, handle_client, (void*)client_socket_ptr) != 0) {
            perror("pthread_create");
            free(client_socket_ptr);
            close(new_socket);
        }

        //On détache le thread pour ne pas faire pthread_join()
        //Le thread va donc libérer lui-même ses ressources à la fin
        pthread_detach(client_thread);
    }

    //Jamais atteint
    close(server_fd);
    return 0;
}
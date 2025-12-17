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
#include <signal.h>

// Variable globale pour contrôler la boucle principale
volatile sig_atomic_t server_running = 1;

ConnectedPlayer clients_list[MAX_CLIENTS];
pthread_mutex_t clients_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t db_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t bc_mutex = PTHREAD_MUTEX_INITIALIZER;

Blockchain *global_blockchain = NULL;

Blockchain* get_global_blockchain() {
    return global_blockchain;
}

int start_server() {
    signal(SIGINT, handle_sigint);
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

    printf("[INFO] Serveur démarré sur le port %d\n", PORT);
    //Boucle d'acceptation des clients
    while (server_running) {
        //La fonction accept() est bloquante et attend une connexion
        if ((new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t*)&addrlen)) < 0) {
            if (!server_running) {
                break;
            }
            perror("accept");
            continue; //Ici on continue juste à écouter
        }

        printf("[INFO] Nouvelle connexion acceptée, socket client : %d\n", new_socket);

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

    close(server_fd);
    printf("[INFO] Socket serveur fermé.\n" );
    return 0;
}

void handle_sigint(int sig) {
    printf("\n[ARRET] Signal reçu, fermeture propre du serveur...\n");
    server_running = 0;
    // Note : sur un accept() bloquant, il faudra peut-être envoyer une fausse connexion 
    // ou utiliser select() pour débloquer la boucle immédiatement, 
    // mais souvent server_running = 0 suffit après la prochaine action.
}
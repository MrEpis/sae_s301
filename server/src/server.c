#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>

#define PORT 8080
#define MAX_PENDING_CONNECTIONS 5


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


int main() {
    int server_fd, new_socket;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);

    //On crée le socket du serveur
    //AF_INET = IPv4, SOCK_STREAM = TCP
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    //Fonction pour rendre le socket réutilisable après un arrêt
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) {
        perror("setsockopt");
        exit(EXIT_FAILURE);
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
        exit(EXIT_FAILURE);
    }

    //On met le port en mode écoute
    if (listen(server_fd, MAX_PENDING_CONNECTIONS) < 0) {
        perror("listen");
        close(server_fd);
        exit(EXIT_FAILURE);
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



}
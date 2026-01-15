#include "server.h"
#include "client_handler.h"
#include "structures.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <sys/time.h>
#include <errno.h>
#include <netinet/in.h>
#include <pthread.h>
#include <signal.h>

// Golobal variable to control main loop
volatile sig_atomic_t server_running = 1;

ConnectedPlayer clients_list[MAX_CLIENTS];
pthread_mutex_t clients_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t db_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t bc_mutex = PTHREAD_MUTEX_INITIALIZER;

Blockchain *global_blockchain = NULL;

Blockchain *get_global_blockchain()
{
    return global_blockchain;
}

/**
 * Main server function.
 * Initializes the server, listens for client messages and creates new threads for each action.
 */
int start_server()
{
    // Look for 'Ctrl+C'
    signal(SIGINT, handle_sigint);

    int server_fd, new_socket;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);

    // Initialize connected clients list
    for (int i = 0; i < MAX_CLIENTS; i++)
    {
        clients_list[i].client_id = -1;
        clients_list[i].socket = -1;
        clients_list[i].logged_in = 0;
    }

    // Create server socket
    // AF_INET = IPv4, SOCK_STREAM = TCP
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
    {
        perror("socket failed");
        return -1;
    }

    // Make socket reusable after server stop
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)))
    {
        perror("setsockopt");
        return -1;
    }

    // Configure address and port
    address.sin_family = AF_INET;         // AF_INET = IPv4
    address.sin_addr.s_addr = INADDR_ANY; // Ecoute sur toutes les interfaces
    // htons() convertit change l'ordre de l'octet en entrée pour correspondre à un octet réseau (avec le bit de poids fort placé en premier)
    address.sin_port = htons(PORT);

    // Bind socket to port
    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address)) < 0)
    {
        perror("bind failed");
        close(server_fd);
        return -1;
    }

    // Make port listen to messages
    if (listen(server_fd, MAX_PENDING_CONNECTIONS) < 0)
    {
        perror("listen");
        close(server_fd);
        return -1;
    }

    printf("[INFO] Server started on port %d\n", PORT);
    // Boucle d'acceptation des clients
    while (server_running)
    {

        fd_set readfs;
        struct timeval tv;

        FD_ZERO(&readfs);
        FD_SET(server_fd, &readfs);

        // Define timeout (1 second)
        tv.tv_sec = 1;
        tv.tv_usec = 0;

        // Wait for activity or time elapse
        int activity = select(server_fd + 1, &readfs, NULL, NULL, &tv);

        // Checks
        if (activity < 0)
        {
            if (errno == EINTR) {
                continue;
            }
            perror("error select");
            break;
        }

        // Timeout reached, go back to start of loop
        if (activity == 0)
        {
            continue;
        }

        // Activity
        if (FD_ISSET(server_fd, &readfs))
        {
            // accept() is blocking and waits for activity
            if ((new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t *)&addrlen)) < 0)
            {
                if (server_running)
                {
                    perror("accept");
                }
                continue;
            }

            printf("[INFO] New connection accepted, client socket: %d\n", new_socket);

            // New thread for this client
            pthread_t client_thread;

            // Allocate memory to pass socket to thread
            // because new_socket will be overwritten in next loop
            int *client_socket_ptr = malloc(sizeof(int));
            if (client_socket_ptr == NULL)
            {
                perror("malloc pour thread");
                close(new_socket);
                continue;
            }
            *client_socket_ptr = new_socket;

            // Create thread on function handle_client()
            if (pthread_create(&client_thread, NULL, handle_client, (void *)client_socket_ptr) != 0)
            {
                perror("pthread_create");
                free(client_socket_ptr);
                close(new_socket);
            }

            // Detach thread so that we don't have to use pthread_join() to rejoin threads manually
            // Thread will free its resources on its own
            pthread_detach(client_thread);
        }
    }

    close(server_fd);
    printf("[INFO] Server socket closed.\n");
    return 0;
}

/**
 * Clean stop of the server if SIGINT signal (Ctrl+C) is sent.
 */
void handle_sigint(int sig)
{
    printf("\n[SHUTDOWN] Signal received, clean server shutdown...\n");
    server_running = 0;
}
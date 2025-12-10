#include "client_handler.h"
#include "protocol.h"
#include "database.h"

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
    ssize_t bytes_read;


    //Boucle de lecture simple en mode bloquant
    
    while ((bytes_read = recv(client_socket, buffer, sizeof(buffer)-1, 0))> 0) { //Si on a bien reçu le message
        buffer[bytes_read] = '\0';
        printf("Message reçu du client %d: %s\n", client_socket, buffer);

        char action_name[50];
        if (extract_json_value(buffer, "nom", action_name, sizeof(action_name))) {
            if (strcmp(action_name, ACTION_LOGIN) == 0) {
                process_login(client_socket, buffer);                
            }
            else if (strcmp(action_name, ACTION_CREATION) == 0) {
                printf("Action CREATION détectée\n");
            }
            else {
                send_error_response(client_socket, action_name, "Action inconnue");
            }
        } else {
            send_error_response(client_socket, "UNKNOWN", "Format JSON invalide");
        }

        // Buffer clean up for next round
        memset(buffer, 0, sizeof(buffer));
    }
    if (bytes_read == 0) {
        printf("Client %d déconnecté.\n", client_socket);
    } else if (bytes_read == -1) {
        perror("recv failed");
    }
    close(client_socket);
    pthread_exit(NULL);
}

void process_login(int client_socket, const char *json_payload) {
    char data_buffer[512];
    char id_str[20];
    char username[50];
    int id_client = -1;

    // 1. Extract "data" object (contains {id_client:..., username:...})
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, "LOGIN", "Champ 'data' manquant");
        return;
    }

    // 2. Extract inner fields
    // ID (mandatory)
    if (extract_json_value(data_buffer, "id_client", id_str, sizeof(id_str))) {
        id_client = atoi(id_str);
    } else {
        // If the client only sends "data": 0
        // Assume data_buffer directly contains the number
        id_client = atoi(data_buffer);
    }

    // Username (mandatory for sign up (ID 0))
    if (!extract_json_value(data_buffer, "username", username, sizeof(username))) {
        // If no username, set it as an empty string
        strcpy(username, "");
    }

    PGconn *conn = get_db_connection();
    int final_id = -1;

    // FOR A NEW USER (ID 0)
    if (id_client == 0) {
        if (strlen(username) < 1) {
            send_error_response(client_socket, "LOGIN", "Username requis pour inscription");
            return;
        }

        int existing_id = db_get_player_id_by_name(conn, username);

        if (existing_id != -1) {
            // First case : this player already exists -> is signed in
            final_id = existing_id;
            printf("Joueur existant '%s' avec ID %d (Login via ID 0)\n", username, final_id);
        } else {
            // Other case : this player doesn't exist yet -> is created
            final_id = db_create_player(conn, username);
            if (final_id == -1) {
                send_error_response(client_socket, "LOGIN", "Erreur création joueur");
                return;
            }
            printf("Nouveau joueur '%s' créé avec ID %d\n", username, final_id);
        }
    }
    // FOR AN EXISTING USER (ID > 0)
    else {
        if (db_player_exists(conn, id_client)) {
            final_id = id_client;
            printf("Joueur ID %d reconnu.\n", final_id);
        } else {
            send_error_response(client_socket, "LOGIN", "Compte introuvable");
            return;
        }
    }

    // SENDING SUCCESS RESPONSE

    // TODO: Get the player's cards
    // For now, return an empty hand "[]"
    char response[1024];
    snprintf(response, sizeof(response), 
    "{\"type\": \"response\", \"nom\": \"LOGIN\", \"status\": \"OK\", \"data\": {\"id_client\": %d, \"username\": \"%s\", \"main\": []}}\n", 
    final_id, username);

    send(client_socket, response, strlen(response), 0);
}
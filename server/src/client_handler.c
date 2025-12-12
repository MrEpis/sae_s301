#include "client_handler.h"
#include "protocol.h"
#include "database.h"
#include "server.h"
#include "blockchain.h"
#include "structures.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/socket.h>
#include <time.h>

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
                process_card_creation(client_socket, buffer);
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

        pthread_mutex_lock(&clients_mutex);
        for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].socket == client_socket) {
            clients_list[i].socket = -1;
            clients_list[i].client_id = -1;
            clients_list[i].logged_in = 0;
            break;
        }
    }
    pthread_mutex_unlock(&clients_mutex);
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

    // SENDING SUCCESS RESPONSE WITH CARDS

    // Adding the player to the list
    pthread_mutex_lock(&clients_mutex);

    int added = 0;
    // Check if player is already connected
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].client_id == final_id) {
            // The player is already connected, refuse connection
            // (we also could disconnect the previous one)
            pthread_mutex_unlock(&clients_mutex);
            send_error_response(client_socket, "LOGIN", "Joueur déjà connecté");
            return;
        }
    }

    // Find free spot
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].socket == -1) { // Is an empty player
            clients_list[i].socket = client_socket;
            clients_list[i].client_id = final_id;
            strcpy(clients_list[i].username, username);
            clients_list[i].logged_in = 1;
            added = -1;
            break;
        }
    }

    pthread_mutex_unlock(&clients_mutex);

    if (!added) {
        send_error_response(client_socket, "LOGIN", "Server at max capacity");
        return;
    }

    char cards_json[4096];
    db_get_player_cards_json(conn, final_id, cards_json, sizeof(cards_json));

    char response[5000];
    snprintf(response, sizeof(response), 
    "{\"type\": \"response\", \"nom\": \"LOGIN\", \"status\": \"OK\", \"data\": {\"id_client\": %d, \"username\": \"%s\", \"main\": %s}}\n", 
    final_id, username, cards_json);

    send(client_socket, response, strlen(response), 0);
}

void process_card_creation(int client_socket, const char *json_payload) {
    char data_buffer[1024];
    char temp_str[100];
    
    // Variables pour stocker les données extraites
    int id_client = -1;
    char nomCarte[50] = {0};
    int attaque = 0;
    int pv = 0;
    char image[50] = {0};

    // 1. Extraction des données du JSON
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, "RequestCardCreation", "Donnees manquantes");
        return;
    }

    // ID Client (qui demande la création)
    if (extract_json_value(data_buffer, "id_client", temp_str, sizeof(temp_str))) {
        id_client = atoi(temp_str);
    }
    
    // Nom de la carte
    extract_json_value(data_buffer, "nomCarte", nomCarte, sizeof(nomCarte));
    
    // Stats (Attaque, PV)
    if (extract_json_value(data_buffer, "attaque", temp_str, sizeof(temp_str))) attaque = atoi(temp_str);
    if (extract_json_value(data_buffer, "pv", temp_str, sizeof(temp_str))) pv = atoi(temp_str);
    
    // Image
    if (!extract_json_value(data_buffer, "image", image, sizeof(image))) {
        strcpy(image, "default.png"); // Image par défaut si non fournie
    }

    // Validation basique
    if (id_client < 1 || strlen(nomCarte) == 0) {
        send_error_response(client_socket, "RequestCardCreation", "Parametres invalides");
        return;
    }

    PGconn *conn = get_db_connection();

    // TODO: Vérifier MaxCardPerClient ici (Compter les cartes du joueur en BDD)
    // int nb_cartes = db_count_player_cards(conn, id_client);
    // if (nb_cartes >= MAX_CARDS) { ... send_error ... return; }

    // 2. Insertion dans la table CARTES (Cache BDD)
    // On met la défense à 0 par défaut pour l'instant
    int new_card_id = db_create_card(conn, id_client, nomCarte, attaque, 0, pv, image);
    
    if (new_card_id == -1) {
        send_error_response(client_socket, "CardCreated", "Erreur base de donnees");
        return;
    }

    printf("Carte crée en BDD (ID: %d) pour le client %d\n", new_card_id, id_client);

    // 3. Création et Minage du BLOC (Blockchain)

    Blockchain *bc = get_global_blockchain();
    if (bc == NULL) {
    send_error_response(client_socket, "CardCreated", "Erreur: Blockchain serveur non chargee");
    return;
    }

    // Allocation du bloc
    Block *new_block = malloc(sizeof(Block));
    if (!new_block) {
        perror("malloc block");
        return;
    }

    new_block->ID_block = bc->tail->ID_block + 1;

    strncpy(new_block->previous_hash, bc->tail->hash, HASH_SIZE);
    new_block->previous_hash[HASH_SIZE - 1] = '\0';

    new_block->timestamp = time(NULL);
    new_block->nonce = 0;

    // Création du JSON pour le bloc (Preuve d'action)
    char action_json[512];
    snprintf(action_json, sizeof(action_json), 
         "{\"action\": \"CreateCard\", \"client_id\": %d, \"card_id\": %d, \"card_name\": \"%s\", \"image_file\": \"%s\"}", 
         id_client, new_card_id, nomCarte, image);
    
    new_block->data_action = strdup(action_json);

    // Minage (Proof of Work)
    mine_block(new_block);

    bc->tail->next = new_block;
    bc->tail = new_block;
    bc->size++;

    // 4. Sauvegarde du bloc miné en BDD
    if (db_save_block(conn, new_block) != 0) {
        fprintf(stderr, "Erreur fatale: Impossible de sauvegarder le bloc !\n");
        // En prod, il faudrait rollback la création de carte ici
    }

    // 5. Réponse au client
    char response[512];
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"CardCreated\", \"status\": \"OK\", \"data\": {\"id\": %d}}\n", 
             new_card_id);
    
    send(client_socket, response, strlen(response), 0);
    
    // Nettoyage mémoire bloc
    //free(new_block->data_action);
    //free(new_block);
}


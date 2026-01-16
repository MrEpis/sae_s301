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


/**
 * Main Client Thread.
 * Reads JSON requests and dispatches them to specific processing functions.
 */
void *handle_client(void *arg) {
    // Get client socket passed as function argument
    int client_socket = *(int*)arg;
    free(arg); 
    
    char buffer[2048];
    ssize_t bytes_read;


    // Main blocking read loop
    
    while ((bytes_read = recv(client_socket, buffer, sizeof(buffer)-1, 0))> 0) { //Si on a bien reçu le message
        buffer[bytes_read] = '\0';
        printf("[CLIENT -> SERVER] (Socket %d) : %s\n", client_socket, buffer);

        char action_name[50];
        if (extract_json_value(buffer, "nom", action_name, sizeof(action_name))) {
            if (strcmp(action_name, ACTION_LOGIN) == 0) {
                process_login(client_socket, buffer);                
            }
            else if (strcmp(action_name, ACTION_CREATION) == 0) {
                process_card_creation(client_socket, buffer);
            }
            else if (strcmp(action_name, ACTION_GET_USERS) == 0) {
                process_get_connected_users(client_socket);
            }
            else if (strcmp(action_name, ACTION_GET_OPPONENT_INVENTORY) == 0) {
                process_get_opponent_inventory(client_socket, buffer);
            }
            else if (strcmp(action_name, ACTION_TRADE) == 0) {
                process_trade_request(client_socket, buffer);
            }
            else if (strcmp(action_name, ACTION_TRADE_RESPONSE) == 0) {
                process_trade_response(client_socket, buffer);
            }
            else if (strcmp(action_name, ACTION_FIGHT) == 0) {
                process_fight_request(client_socket, buffer);
            }
            else if (strcmp(action_name, ACTION_FIGHT_RESPONSE) == 0) {
                process_fight_response(client_socket, buffer);
            }
            else {
                send_error_response(client_socket, action_name, "Unknown action.");
            }
        } else {
            send_error_response(client_socket, "UNKNOWN", "Invalid JSON format");
        }

        // Buffer clean up for next round
        memset(buffer, 0, sizeof(buffer));
    }
    if (bytes_read == 0) {
        printf("[INFO] Client disconnected (Socket %d)\n", client_socket);

        pthread_mutex_lock(&clients_mutex);
        for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].socket == client_socket) {
            clients_list[i].socket = -1;
            clients_list[i].client_id = -1;
            clients_list[i].logged_in = 0;
            break;
        }
    }
    } else if (bytes_read == -1) {
        perror("[INFO] recv failed (Client déconnecté)\n");
        for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].socket == client_socket) {
            clients_list[i].socket = -1;
            clients_list[i].client_id = -1;
            clients_list[i].logged_in = 0;
            break;
        }
    }
    }
    pthread_mutex_unlock(&clients_mutex);
    close(client_socket);
    pthread_exit(NULL);
}

/**
 * Handles the LOGIN action.
 * Checks credentials, creates user if needed, and returns the current hand.
 */
void process_login(int client_socket, const char *json_payload) {
    char data_buffer[512];
    char id_str[20];
    char username[50];
    int id_client = -1;

    // Extract "data" segment
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, "LOGIN", "Missing 'data' field");
        return;
    }

    // Extract inner fields
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
            send_error_response(client_socket, "LOGIN", "Username is required to sign up.");
            return;
        }

        int existing_id = db_get_player_id_by_name(conn, username);

        if (existing_id != -1) {
            // First case : this player already exists -> is signed in
            final_id = existing_id;
            printf("[INFO] Existing player '%s' with ID %d (Logged in using ID 0)\n", username, final_id);
        } else {
            // Other case : this player doesn't exist yet -> is created
            final_id = db_create_player(conn, username);
            if (final_id == -1) {
                send_error_response(client_socket, "LOGIN", "Error while creating player in DB");
                return;
            }
            printf("[INFO] New player '%s' created with ID %d\n", username, final_id);
        }
    }
    // FOR AN EXISTING USER (ID > 0)
    else {
        if (db_player_exists(conn, id_client)) {
            final_id = id_client;

            if (db_get_username_by_id(conn, final_id, username) == -1) {
                 send_error_response(client_socket, "LOGIN", "Error while fetching username");
                 return;
            }
            printf("Joueur ID %d reconnu.\n", final_id);
        } else {
            send_error_response(client_socket, "LOGIN", "Account does not exist");
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
            send_error_response(client_socket, "LOGIN", "Player already connected");
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

    printf("[SERVER -> CLIENT] (Socket %d) : %s\n", client_socket, response);

    send(client_socket, response, strlen(response), 0);
}

/**
 * Handles Card Creation request.
 * Generates card, saves to DB, mines a block.
 */
void process_card_creation(int client_socket, const char *json_payload) {
    char data_buffer[1024];
    char temp_str[100];
    
    int id_client = -1;
    char nomCarte[50] = {0};
    int defense = 0;
    int attaque = 0;
    int pv = 0;
    char image[50] = {0};

    // Extract data from JSON
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, "RequestCardCreation", "Donnees manquantes");
        return;
    }

    if (extract_json_value(data_buffer, "id_client", temp_str, sizeof(temp_str))) {
        id_client = atoi(temp_str);
    }
    
    extract_json_value(data_buffer, "nomCarte", nomCarte, sizeof(nomCarte));
    
    if (extract_json_value(data_buffer, "attaque", temp_str, sizeof(temp_str))) attaque = atoi(temp_str);
    if (extract_json_value(data_buffer, "pv", temp_str, sizeof(temp_str))) pv = atoi(temp_str);
    if (extract_json_value(data_buffer, "defense", temp_str, sizeof(temp_str))) defense = atoi(temp_str);
    // Image
    if (!extract_json_value(data_buffer, "image", image, sizeof(image))) {
        strcpy(image, "default.png");
    }

    if (id_client < 1 || strlen(nomCarte) == 0) {
        send_error_response(client_socket, "RequestCardCreation", "Parametres invalides");
        return;
    }

    PGconn *conn = get_db_connection();

    int nb_cartes = db_count_player_cards(conn, id_client);
    if (nb_cartes >= MAX_CARDS) { 
        send_error_response(client_socket, "RequestCardCreation", "Nombre de cartes max atteint");
        return;
     }

    // Insert card in DB
    int new_card_id = db_create_card(conn, id_client, nomCarte, attaque, defense, pv, image);
    
    if (new_card_id == -1) {
        send_error_response(client_socket, "CardCreated", "Erreur base de donnees");
        return;
    }

    printf("[INFO] Card created in DB (ID: %d) for player %d\n", new_card_id, id_client);

    // Create and mine Block

    Blockchain *bc = get_global_blockchain();
    if (bc == NULL) {
    send_error_response(client_socket, "CardCreated", "Erreur: Blockchain serveur non chargee");
    return;
    }

    Block *new_block = calloc(1, sizeof(Block));
    if (new_block == NULL) {
        perror("calloc failed");
        return;
    }

    new_block->ID_block = bc->tail->ID_block + 1;

    strncpy(new_block->previous_hash, bc->tail->hash, HASH_SIZE);
    new_block->previous_hash[HASH_SIZE - 1] = '\0';

    new_block->timestamp = time(NULL);
    new_block->nonce = 0;

    char action_json[512];
    snprintf(action_json, sizeof(action_json), 
         "{\"action\": \"CreateCard\", \"client_id\": %d, \"card_id\": %d, \"card_name\": \"%s\", \"attack\": %d, \"defense\": %d, \"max_hp\": %d, \"image_file\": \"%s\"}", 
         id_client, new_card_id, nomCarte, attaque, defense, pv, image);
    
    new_block->data_action = strdup(action_json);

    mine_block(new_block);

    bc->tail->next = new_block;
    bc->tail = new_block;
    bc->size++;

    // Save block in DB
    if (db_save_block(conn, new_block) != 0) {
        fprintf(stderr, "[ERROR] Error while saving block\n");
    }

    // Send response to client
    char response[512];
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"CardCreated\", \"status\": \"OK\", \"data\": {\"id\": %d}}\n", 
             new_card_id);

    printf("[SERVER -> CLIENT] (client socket %d) : %s", client_socket, response);
    
    send(client_socket, response, strlen(response), 0);
}

/**
 * Returns the list of connected users (excluding self).
 */
void process_get_connected_users(int client_socket) {
    char json_array[4096] = "[";
    int first = 1;

    pthread_mutex_lock(&clients_mutex);

    // Find requester ID
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].socket != -1 && clients_list[i].logged_in == 1) {
            
            if (!first) {
                strcat(json_array, ",");
            }
            
            char user_entry[256];
            snprintf(user_entry, sizeof(user_entry), 
                     "{\"id_client\": %d, \"username\": \"%s\"}", 
                     clients_list[i].client_id, 
                     clients_list[i].username);

            strcat(json_array, user_entry);
            first = 0;
        }
    }

    pthread_mutex_unlock(&clients_mutex);

    strcat(json_array, "]");

    // Sending response
    char response[5000];
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"OK\", \"data\": %s}\n", 
             ACTION_GET_USERS, json_array);
    printf("[SERVER -> CLIENT] (client socket %d) : %s", client_socket, response);
    fflush(stdout);

    send(client_socket, response, strlen(response), 0);
}

/**
 * Returns the inventory of the selected player.
 */
void process_get_opponent_inventory(int client_socket, const char *json_payload) {
    char data_buffer[512];
    char target_username[50];
    
    // Extract "data" field
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, ACTION_GET_OPPONENT_INVENTORY, "Donnees manquantes");
        return;
    }

    // Extract username
    if (!extract_json_value(data_buffer, "username", target_username, sizeof(target_username))) {
        send_error_response(client_socket, ACTION_GET_OPPONENT_INVENTORY, "Username manquant");
        return;
    }

    PGconn *conn = get_db_connection();

    // Get user ID by username
    int target_id = db_get_player_id_by_name(conn, target_username);
    
    if (target_id == -1) {
        send_error_response(client_socket, ACTION_GET_OPPONENT_INVENTORY, "Joueur introuvable");
        return;
    }

    // Get their inventory
    char cards_json[4096];
    db_get_player_cards_json(conn, target_id, cards_json, sizeof(cards_json));

    // Send response
    char response[5000];
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"OK\", \"data\": %s}\n", 
             ACTION_GET_OPPONENT_INVENTORY, cards_json);

    printf("[SERVER -> CLIENT] (client socket %d) Inventory of player %s sent: %s",
           client_socket, target_username, response);
    fflush(stdout);

    send(client_socket, response, strlen(response), 0);
}   

/**
 * Handles trade request.
 * If receiving player found, sends them the trade offer.
 */
void process_trade_request(int client_socket, const char *json_payload) {
    char data_buffer[512];
    char temp_str[50];
    
    int id_initiator = -1;
    int id_card_initiator = -1;
    int id_receiver = -1;
    int id_card_receiver = -1;

    // Data extraction
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, ACTION_TRADE, "Donnees manquantes");
        return;
    }

    if (extract_json_value(data_buffer, "id_initiator", temp_str, sizeof(temp_str))) id_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_initiator", temp_str, sizeof(temp_str))) id_card_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_receiver", temp_str, sizeof(temp_str))) id_receiver = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_receiver", temp_str, sizeof(temp_str))) id_card_receiver = atoi(temp_str);

    if (id_initiator == -1 || id_receiver == -1 || id_card_initiator == -1 || id_card_receiver == -1) {
        send_error_response(client_socket, ACTION_TRADE, "IDs invalides");
        return;
    }

    PGconn *conn = get_db_connection();

    if (db_card_exists(conn, id_card_initiator) == 0) {
        send_error_response(client_socket, ACTION_TRADE, "Votre carte n'existe plus.");
        return;
    }
    if (db_card_exists(conn, id_card_receiver) == 0) {
        send_error_response(client_socket, ACTION_TRADE, "La carte que vous cherchez n'existe plus.");
        return;
    }

    // Search for the trade receiver's socket
    int socket_receiver = -1;
    char receiver_username[50] = "Inconnu";

    pthread_mutex_lock(&clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        // On cherche le joueur connecté qui correspond à id_receiver
        if (clients_list[i].logged_in && clients_list[i].client_id == id_receiver) {
            socket_receiver = clients_list[i].socket;
            strcpy(receiver_username, clients_list[i].username);
            break;
        }
    }
    pthread_mutex_unlock(&clients_mutex);

    // If player not found
    if (socket_receiver == -1) {
        send_error_response(client_socket, ACTION_TRADE, "Le joueur cible n'est pas connecte");
        return;
    }

    // Build "ConfirmationRequest" message to trade offerer
    char notification[1024];
    snprintf(notification, sizeof(notification), 
             "{\"type\": \"request\", \"nom\": \"%s\", \"data\": {\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_card_receiver\": %d}}\n",
             ACTION_CONFIRMATION, id_initiator, id_card_initiator, id_card_receiver);

    // Send offer to receiver
    printf("[SERVER -> CLIENT %d (Trade receiver)] Sending trade offer: %s", socket_receiver, notification);
    send(socket_receiver, notification, strlen(notification), 0);

    // Send confirmation to offerer
    char ack[256];
    snprintf(ack, sizeof(ack), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"OK\", \"data\": \"Offre envoyee a %s\"}\n", 
             ACTION_TRADE, receiver_username);
    
    printf("[SERVER -> CLIENT %d (Trade initiator)] Trade request confirmation: %s", client_socket, ack);
    send(client_socket, ack, strlen(ack), 0);
}

/**
 * Handles the trade's response.
 * If accepted, executes the trade in DB and mines a block.
 */
void process_trade_response(int client_socket, const char *json_payload) {
    char data_buffer[1024];
    char temp_str[50];
    char status_str[20];

    int id_initiator = -1;
    int id_card_initiator = -1;
    int id_receiver = -1;
    int id_card_receiver = -1;
    int accepted = 0;

    // Data extraction
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) return;

    // Check if trade is accepted or refused
    if (extract_json_value(data_buffer, "accepted", status_str, sizeof(status_str))) {
        if (strcmp(status_str, "true") == 0) accepted = 1;
    }

    if (extract_json_value(data_buffer, "id_initiator", temp_str, sizeof(temp_str))) id_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_initiator", temp_str, sizeof(temp_str))) id_card_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_receiver", temp_str, sizeof(temp_str))) id_receiver = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_receiver", temp_str, sizeof(temp_str))) id_card_receiver = atoi(temp_str);

    // Find trade initiator's socket
    int socket_initiator = -1;
    pthread_mutex_lock(&clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].logged_in && clients_list[i].client_id == id_initiator) {
            socket_initiator = clients_list[i].socket;
            break;
        }
    }
    pthread_mutex_unlock(&clients_mutex);

    // Processing
    if (!accepted) {
        // If trade refused, send message to the initiator
        if (socket_initiator != -1) {
            char msg[] = "{\"type\": \"response\", \"nom\": \"TradeResult\", \"status\": \"ERROR\", \"data\": \"Echange refuse par le joueur\"}\n";
            send(socket_initiator, msg, strlen(msg), 0);
        }
        return;
    }

    // If accepted, first connect to db
    PGconn *conn = get_db_connection();

    // Checks
    if (db_card_exists(conn, id_card_initiator) == 0) {
        send_error_response(client_socket, ACTION_TRADE_RESPONSE, "La carte de l'initiateur n'existe plus.");
        return;
    }
    if (db_card_exists(conn, id_card_receiver) == 0) {
        send_error_response(client_socket, ACTION_TRADE_RESPONSE, "Votre carte n'existe plus.");
        return;
    }
    
    // Update db
    if (db_execute_trade(conn, id_initiator, id_card_initiator, id_receiver, id_card_receiver) != 0) {
        send_error_response(client_socket, "TradeResponse", "Echec technique de l'echange en BDD");
        return;
    }

    // Add in Blockchain
    Blockchain *bc = get_global_blockchain();
    Block *new_block = calloc(1, sizeof(Block));
    if (new_block == NULL) {
        perror("calloc failed");
        return;
    }
    new_block->ID_block = bc->tail->ID_block + 1;
    new_block->timestamp = time(NULL);
    new_block->nonce = 0;
    strncpy(new_block->previous_hash, bc->tail->hash, HASH_SIZE);
    
    char action_json[512];
    snprintf(action_json, sizeof(action_json), 
         "{\"action\": \"Trade\", \"p1\": %d, \"card1\": %d, \"p2\": %d, \"card2\": %d}", 
         id_initiator, id_card_initiator, id_receiver, id_card_receiver);
    new_block->data_action = strdup(action_json);
    
    mine_block(new_block);
    
    bc->tail->next = new_block;
    bc->tail = new_block;
    bc->size++;
    
    db_save_block(conn, new_block);

    // Send sucess messages with updated inventories
    
    // 1. For the trade receiver
    // Get updated hand
    char hand_recv[4096];
    db_get_player_cards_json(conn, id_receiver, hand_recv, sizeof(hand_recv));
    
    char msg_b[5000];
    // Send message
    snprintf(msg_b, sizeof(msg_b), 
             "{\"type\": \"response\", \"nom\": \"TradeResult\", \"status\": \"OK\", \"data\": {\"message\": \"Echange valide\", \"hand\": %s}}\n", 
             hand_recv);

    printf("[SERVER -> CLIENT %d] Send message with new hand after trade\n", client_socket);
    send(client_socket, msg_b, strlen(msg_b), 0); 

    // 2. For the trade initiator
    // Same thing
    if (socket_initiator != -1) {
        char hand_init[4096];
        db_get_player_cards_json(conn, id_initiator, hand_init, sizeof(hand_init));
        
        char msg_a[5000];
        snprintf(msg_a, sizeof(msg_a), 
                 "{\"type\": \"response\", \"nom\": \"TradeResult\", \"status\": \"OK\", \"data\": {\"message\": \"Offre acceptee\", \"hand\": %s}}\n", 
                 hand_init);
        
        printf("[SERVER -> CLIENT %d] Send message with new hand after trade\n", socket_initiator);
        send(socket_initiator, msg_a, strlen(msg_a), 0);
    }

    printf("[INFO] Trade succesfully executed.\n");
}

/**
 * Handles fight request.
 * Sends offer to requested player.
 */
void process_fight_request(int client_socket, const char *json_payload) {
    char data_buffer[512];
    char temp_str[50];
    
    int id_initiator = -1;
    int id_card_initiator = -1;
    int id_receiver = -1;
    int id_card_receiver = -1;

    // Extract data
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, ACTION_FIGHT, "Donnees manquantes");
        return;
    }

    if (extract_json_value(data_buffer, "id_initiator", temp_str, sizeof(temp_str))) id_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_initiator", temp_str, sizeof(temp_str))) id_card_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_receiver", temp_str, sizeof(temp_str))) id_receiver = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_receiver", temp_str, sizeof(temp_str))) id_card_receiver = atoi(temp_str);

    if (id_initiator == -1 || id_receiver == -1) {
        send_error_response(client_socket, ACTION_FIGHT, "IDs invalides");
        return;
    }

    PGconn *conn = get_db_connection();

    if (db_card_exists(conn, id_card_initiator) == 0) {
        send_error_response(client_socket, ACTION_FIGHT, "Votre carte n'existe plus.");
        return;
    }
    if (db_card_exists(conn, id_card_receiver) == 0) {
        send_error_response(client_socket, ACTION_FIGHT, "La carte que vous cherchez n'existe plus.");
        return;
    }

    // Search opponent socket
    int socket_receiver = -1;
    char receiver_username[50] = "Inconnu";

    pthread_mutex_lock(&clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].logged_in && clients_list[i].client_id == id_receiver) {
            socket_receiver = clients_list[i].socket;
            strcpy(receiver_username, clients_list[i].username);
            break;
        }
    }
    pthread_mutex_unlock(&clients_mutex);

    // If player not connected
    if (socket_receiver == -1) {
        send_error_response(client_socket, ACTION_FIGHT, "L'adversaire n'est pas connecte");
        return;
    }

    // Send offer to opponent
    char notification[1024];
    snprintf(notification, sizeof(notification), 
             "{\"type\": \"request\", \"nom\": \"%s\", \"data\": {\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_card_receiver\": %d}}\n",
             ACTION_FIGHT_CONFIRMATION, id_initiator, id_card_initiator, id_card_receiver);

    printf("[SERVER -> CLIENT %d (%s)] Sending fight request to player %s", socket_receiver, receiver_username, notification);
    fflush(stdout);
    send(socket_receiver, notification, strlen(notification), 0);

    // Send confirmation to initiator
    char ack[256];
    snprintf(ack, sizeof(ack), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"OK\", \"data\": \"Defi envoye a %s\"}\n", 
             ACTION_FIGHT, receiver_username);
    
    printf("[SERVER -> CLIENT %d] Sent fight request from %s", client_socket, ack);
    send(client_socket, ack, strlen(ack), 0);
}


/**
 * Handles response to fight offer.
 * If accepted, executes the fight, updates DB and mines block.
 */
void process_fight_response(int client_socket, const char *json_payload) {
    char data_buffer[1024];
    char temp_str[50];
    int accepted = 0;

    int id_initiator = -1, id_card_initiator = -1;
    int id_receiver = -1, id_card_receiver = -1;

    // Extract data
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) return;

    if (strstr(data_buffer, "\"accepted\": true") != NULL) accepted = 1;

    if (extract_json_value(data_buffer, "id_initiator", temp_str, sizeof(temp_str))) id_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_initiator", temp_str, sizeof(temp_str))) id_card_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_receiver", temp_str, sizeof(temp_str))) id_receiver = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_receiver", temp_str, sizeof(temp_str))) id_card_receiver = atoi(temp_str);

    // Search fight initiator socket
    int socket_initiator = -1;
    pthread_mutex_lock(&clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].logged_in && clients_list[i].client_id == id_initiator) {
            socket_initiator = clients_list[i].socket;
            break;
        }
    }
    pthread_mutex_unlock(&clients_mutex);

    // If fight refused, only send message to initiator and return
    if (!accepted) {
        if (socket_initiator != -1) {
            char msg[] = "{\"type\": \"response\", \"nom\": \"FightResult\", \"status\": \"ERROR\", \"data\": \"Combat refuse\"}\n";
            send(socket_initiator, msg, strlen(msg), 0);
        }
        return;
    }

    // Fight processing
    PGconn *conn = get_db_connection();

    // Checks
    if (db_card_exists(conn, id_card_initiator) == 0) {
        send_error_response(client_socket, ACTION_FIGHT_RESPONSE, "La carte de l'initiateur n'existe plus.");
        return;
    }
    if (db_card_exists(conn, id_card_receiver) == 0) {
        send_error_response(client_socket, ACTION_FIGHT_RESPONSE, "Votre carte n'existe plus.");
        return;
    }

    char json_card_initiator[1024];
    char json_card_receiver[1024];

    int atk1, def1, hp1, atk2, def2, hp2;

    // Get card stats
    if (db_get_card_stats(conn, id_card_initiator, &atk1, &def1, &hp1) != 0 ||
        db_get_card_stats(conn, id_card_receiver, &atk2, &def2, &hp2) != 0) {
        return; 
    }

    float dmg_to_2 = atk1 - (atk1 * (def2/100.0));
    float dmg_to_1 = atk2 - (atk2 * (def1/100.0));

    if (dmg_to_2 < 0) dmg_to_2 = 0;
    if (dmg_to_1 < 0) dmg_to_1 = 0;

    // Dealing damage
    int new_hp1 = hp1 - ((int) dmg_to_1);
    int new_hp2 = hp2 - ((int) dmg_to_2);

    // Update db (deletes card if killed)
    int status1 = db_update_card_hp(conn, id_card_initiator, new_hp1);
    int status2 = db_update_card_hp(conn, id_card_receiver, new_hp2);

    // Mine block
    Blockchain *bc = get_global_blockchain();
    Block *new_block = calloc(1, sizeof(Block));
    if (new_block == NULL) {
        perror("calloc failed");
        return;
    }
    new_block->ID_block = bc->tail->ID_block + 1;
    new_block->timestamp = time(NULL);
    new_block->nonce = 0;
    strncpy(new_block->previous_hash, bc->tail->hash, HASH_SIZE);

    char action_json[512];

    // Winner is either the one that dealt the most damage OR the one who killed the opponent's card
    int winner_id;
    if ((status1 && status2) || (!status1 && !status2)) {
        winner_id = (dmg_to_2 > dmg_to_1) ? id_initiator : id_receiver;
    } else if (!status1 && status2) {
        winner_id = id_receiver;
    } else if (status1 && !status2) {
        winner_id = id_initiator;
    }

    snprintf(action_json, sizeof(action_json), 
         "{\"action\": \"Fight\", \"p1\": %d, \"dmg1\": %d, \"p2\": %d, \"dmg2\": %d, \"winner\": %d}", 
         id_initiator, ((int) dmg_to_1), id_receiver, ((int) dmg_to_2), winner_id);
    
    new_block->data_action = strdup(action_json);
    mine_block(new_block);
    
    bc->tail->next = new_block;
    bc->tail = new_block;
    bc->size++;
    db_save_block(conn, new_block);

    // Send results with updated hands
    char hand_init[4096], hand_recv[4096];
    db_get_player_cards_json(conn, id_initiator, hand_init, sizeof(hand_init));
    db_get_player_cards_json(conn, id_receiver, hand_recv, sizeof(hand_recv));

    const char *res_receiver = (id_receiver == winner_id) ? "VICTOIRE" : "DEFAITE";
    const char *res_initiator = (id_initiator == winner_id) ? "VICTOIRE" : "DEFAITE";

    char result_msg[6000];
    
    db_get_single_card_json(conn, id_card_initiator, json_card_initiator, sizeof(json_card_initiator));
    db_get_single_card_json(conn, id_card_receiver, json_card_receiver, sizeof(json_card_receiver));    
    
    // Message for receiver

    snprintf(result_msg, sizeof(result_msg), 
             "{\"type\": \"response\", \"nom\": \"FightResult\", \"status\": \"OK\", \"data\": {\"result\": \"%s\", \"log\": \"[%s] Combat termine. Tu as subi %d degats. Tu as inflige %d degats.\", \"hand\": %s, \"opponent_card\": %s}}\n", 
             res_receiver, res_receiver, ((int) dmg_to_2), ((int) dmg_to_1), hand_recv, json_card_initiator);
    send(client_socket, result_msg, strlen(result_msg), 0);

    // Message for initiator
    if (socket_initiator != -1) {
        snprintf(result_msg, sizeof(result_msg), 
             "{\"type\": \"response\", \"nom\": \"FightResult\", \"status\": \"OK\", \"data\": {\"result\": \"%s\", \"log\": \"[%s] Combat termine. Tu as subi %d degats. Tu as inflige %d degats.\", \"hand\": %s, \"opponent_card\": %s}}\n", 
             res_initiator, res_initiator, ((int) dmg_to_1), ((int) dmg_to_2), hand_init, json_card_receiver);
        send(socket_initiator, result_msg, strlen(result_msg), 0);
    }

    printf("[INFO] Fight finished : %d vs %d (Dmg: %d - %d). Winner: %d\n", id_initiator, id_receiver, ((int) dmg_to_1), ((int) dmg_to_2), winner_id);
}
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
        printf("[CLIENT -> SERVEUR] (Socket client %d) : %s\n", client_socket, buffer);

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
                send_error_response(client_socket, action_name, "Action inconnue");
            }
        } else {
            send_error_response(client_socket, "UNKNOWN", "Format JSON invalide");
        }

        // Buffer clean up for next round
        memset(buffer, 0, sizeof(buffer));
    }
    if (bytes_read == 0) {
        printf("[INFO] Socket client %d déconnecté.\n", client_socket);

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
            printf("[INFO] Joueur existant '%s' avec ID %d (Login via ID 0)\n", username, final_id);
        } else {
            // Other case : this player doesn't exist yet -> is created
            final_id = db_create_player(conn, username);
            if (final_id == -1) {
                send_error_response(client_socket, "LOGIN", "Erreur création joueur");
                return;
            }
            printf("[INFO] Nouveau joueur '%s' créé avec ID %d\n", username, final_id);
        }
    }
    // FOR AN EXISTING USER (ID > 0)
    else {
        if (db_player_exists(conn, id_client)) {
            final_id = id_client;

            if (db_get_username_by_id(conn, final_id, username) == -1) {
                 send_error_response(client_socket, "LOGIN", "Erreur récupération nom");
                 return;
            }
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

    printf("[SERVEUR -> CLIENT] (Socket client %d) : %s\n", client_socket, response);

    send(client_socket, response, strlen(response), 0);
}

void process_card_creation(int client_socket, const char *json_payload) {
    char data_buffer[1024];
    char temp_str[100];
    
    // Variables pour stocker les données extraites
    int id_client = -1;
    char nomCarte[50] = {0};
    int defense = 0;
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
    
    // Stats (Attaque, PV, Défense)
    if (extract_json_value(data_buffer, "attaque", temp_str, sizeof(temp_str))) attaque = atoi(temp_str);
    if (extract_json_value(data_buffer, "pv", temp_str, sizeof(temp_str))) pv = atoi(temp_str);
    if (extract_json_value(data_buffer, "defense", temp_str, sizeof(temp_str))) defense = atoi(temp_str);
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
    int nb_cartes = db_count_player_cards(conn, id_client);
    if (nb_cartes >= MAX_CARDS) { 
        send_error_response(client_socket, "RequestCardCreation", "Nombre de cartes max atteint");
        return;
     }

    // 2. Insertion dans la table CARTES (Cache BDD)
    int new_card_id = db_create_card(conn, id_client, nomCarte, attaque, defense, pv, image);
    
    if (new_card_id == -1) {
        send_error_response(client_socket, "CardCreated", "Erreur base de donnees");
        return;
    }

    printf("[INFO] Carte crée en BDD (ID: %d) pour le joueur %d\n", new_card_id, id_client);

    // 3. Création et Minage du BLOC (Blockchain)

    Blockchain *bc = get_global_blockchain();
    if (bc == NULL) {
    send_error_response(client_socket, "CardCreated", "Erreur: Blockchain serveur non chargee");
    return;
    }

    // Allocation du bloc
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

    // Création du JSON pour le bloc (Preuve d'action)
    char action_json[512];
    snprintf(action_json, sizeof(action_json), 
         "{\"action\": \"CreateCard\", \"client_id\": %d, \"card_id\": %d, \"card_name\": \"%s\", \"attack\": %d, \"defense\": %d, \"max_hp\": %d, \"image_file\": \"%s\"}", 
         id_client, new_card_id, nomCarte, attaque, defense, pv, image);
    
    new_block->data_action = strdup(action_json);

    // Minage (Proof of Work)
    mine_block(new_block);

    bc->tail->next = new_block;
    bc->tail = new_block;
    bc->size++;

    // 4. Sauvegarde du bloc miné en BDD
    if (db_save_block(conn, new_block) != 0) {
        fprintf(stderr, "[ERREUR] Impossible de sauvegarder le bloc\n");
        // En prod, il faudrait rollback la création de carte ici
    }

    // 5. Réponse au client
    char response[512];
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"CardCreated\", \"status\": \"OK\", \"data\": {\"id\": %d}}\n", 
             new_card_id);

    printf("[SERVEUR -> CLIENT] (socket client %d) : %s", client_socket, response);
    
    send(client_socket, response, strlen(response), 0);
}

void process_get_connected_users(int client_socket) {
    char json_array[4096] = "["; // Buffer large pour la liste
    int first = 1;

    pthread_mutex_lock(&clients_mutex); // Verrouillage pour lecture

    for (int i = 0; i < MAX_CLIENTS; i++) {
        // On ne liste que les sockets valides et les joueurs authentifiés (logged_in)
        if (clients_list[i].socket != -1 && clients_list[i].logged_in == 1) {
            
            // Gestion de la virgule entre les éléments JSON
            if (!first) {
                strcat(json_array, ",");
            }
            
            char user_entry[256];
            // Format JSON : {"id_client": 1, "username": "leo"}
            // On utilise id_client pour correspondre au champ de votre structure
            snprintf(user_entry, sizeof(user_entry), 
                     "{\"id_client\": %d, \"username\": \"%s\"}", 
                     clients_list[i].client_id, 
                     clients_list[i].username);
            
            // Concaténation sécurisée (vérifier limites en prod, ici simplifié)
            strcat(json_array, user_entry);
            first = 0;
        }
    }

    pthread_mutex_unlock(&clients_mutex); // Déverrouillage

    strcat(json_array, "]"); // Fermeture du tableau

    // Envoi de la réponse au format standard
    char response[5000];
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"OK\", \"data\": %s}\n", 
             ACTION_GET_USERS, json_array);
    printf("[SERVEUR -> CLIENT] (Socket client %d) : %s", client_socket, response);
    fflush(stdout);

    send(client_socket, response, strlen(response), 0);
}

void process_get_opponent_inventory(int client_socket, const char *json_payload) {
    char data_buffer[512];
    char target_username[50];
    
    // 1. Extraire l'objet "data"
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, ACTION_GET_OPPONENT_INVENTORY, "Donnees manquantes");
        return;
    }

    // 2. Extraire le "username" cible
    if (!extract_json_value(data_buffer, "username", target_username, sizeof(target_username))) {
        send_error_response(client_socket, ACTION_GET_OPPONENT_INVENTORY, "Username manquant");
        return;
    }

    PGconn *conn = get_db_connection();

    // 3. Trouver l'ID du joueur cible via son pseudo
    int target_id = db_get_player_id_by_name(conn, target_username);
    
    if (target_id == -1) {
        send_error_response(client_socket, ACTION_GET_OPPONENT_INVENTORY, "Joueur introuvable");
        return;
    }

    // 4. Récupérer son inventaire
    char cards_json[4096];
    db_get_player_cards_json(conn, target_id, cards_json, sizeof(cards_json));

    // 5. Construire et envoyer la réponse
    char response[5000];
    snprintf(response, sizeof(response), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"OK\", \"data\": %s}\n", 
             ACTION_GET_OPPONENT_INVENTORY, cards_json);

    printf("[SERVEUR -> CLIENT] (Socket client %d) Inventaire de %s envoyé : %s", 
           client_socket, target_username, response);
    fflush(stdout);

    send(client_socket, response, strlen(response), 0);
}   

void process_trade_request(int client_socket, const char *json_payload) {
    char data_buffer[512];
    char temp_str[50];
    
    int id_initiator = -1;
    int id_card_initiator = -1;
    int id_receiver = -1;
    int id_card_receiver = -1;

    // 1. Extraction des données
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, ACTION_TRADE, "Donnees manquantes");
        return;
    }

    if (extract_json_value(data_buffer, "id_initiator", temp_str, sizeof(temp_str))) id_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_initiator", temp_str, sizeof(temp_str))) id_card_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_receiver", temp_str, sizeof(temp_str))) id_receiver = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_receiver", temp_str, sizeof(temp_str))) id_card_receiver = atoi(temp_str);

    // Vérification basique
    if (id_initiator == -1 || id_receiver == -1) {
        send_error_response(client_socket, ACTION_TRADE, "IDs invalides");
        return;
    }

    // 2. Recherche du socket du destinataire (Client B)
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

    // 3. Gestion du cas "Joueur non trouvé"
    if (socket_receiver == -1) {
        send_error_response(client_socket, ACTION_TRADE, "Le joueur cible n'est pas connecte");
        return;
    }

    // 4. Construction du message pour le destinataire (Client B)
    // On lui envoie une "ConfirmationRequest"
    char notification[1024];
    snprintf(notification, sizeof(notification), 
             "{\"type\": \"request\", \"nom\": \"%s\", \"data\": {\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_card_receiver\": %d}}\n",
             ACTION_CONFIRMATION, id_initiator, id_card_initiator, id_card_receiver);

    // 5. Envoi au destinataire
    printf("[SERVEUR -> CLIENT %d (Trade receiver)] Transfert offre echange : %s", socket_receiver, notification);
    send(socket_receiver, notification, strlen(notification), 0);

    // 6. Confirmation à l'expéditeur (Client A)
    char ack[256];
    snprintf(ack, sizeof(ack), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"OK\", \"data\": \"Offre envoyee a %s\"}\n", 
             ACTION_TRADE, receiver_username);
    
    printf("[SERVEUR -> CLIENT %d (Trade initiator)] Ack echange : %s", client_socket, ack);
    send(client_socket, ack, strlen(ack), 0);
}

// server/src/client_handler.c

void process_trade_response(int client_socket, const char *json_payload) {
    char data_buffer[1024];
    char temp_str[50];
    char status_str[20];

    int id_initiator = -1;
    int id_card_initiator = -1;
    int id_receiver = -1;
    int id_card_receiver = -1;
    int accepted = 0;

    // 1. Extraction des données
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) return;

    // On regarde si c'est accepté ou refusé ("accepted": true/false ou "response": "ACCEPTED")
    // Adapté selon votre JSON client. Ici je suppose un champ "response": "ACCEPTED"
    if (extract_json_value(data_buffer, "accepted", status_str, sizeof(status_str))) {
        if (strcmp(status_str, "true") == 0) accepted = 1;
    }

    if (extract_json_value(data_buffer, "id_initiator", temp_str, sizeof(temp_str))) id_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_initiator", temp_str, sizeof(temp_str))) id_card_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_receiver", temp_str, sizeof(temp_str))) id_receiver = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_receiver", temp_str, sizeof(temp_str))) id_card_receiver = atoi(temp_str);

    // 2. Retrouver le socket de l'initiateur (Celui qui a proposé l'échange)
    int socket_initiator = -1;
    pthread_mutex_lock(&clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].logged_in && clients_list[i].client_id == id_initiator) {
            socket_initiator = clients_list[i].socket;
            break;
        }
    }
    pthread_mutex_unlock(&clients_mutex);

    // 3. Traitement
    if (!accepted) {
        // CAS REFUSÉ
        if (socket_initiator != -1) {
            char msg[] = "{\"type\": \"response\", \"nom\": \"TradeResult\", \"status\": \"ERROR\", \"data\": \"Echange refuse par le joueur\"}\n";
            send(socket_initiator, msg, strlen(msg), 0);
        }
        return;
    }

    // CAS ACCEPTÉ
    PGconn *conn = get_db_connection();
    
    // A. Mise à jour BDD (Transaction)
    if (db_execute_trade(conn, id_initiator, id_card_initiator, id_receiver, id_card_receiver) != 0) {
        send_error_response(client_socket, "TradeResponse", "Echec technique de l'echange en BDD");
        return;
    }

    // B. Ajout dans la Blockchain
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
    
    db_save_block(conn, new_block); // Sauvegarde persistante

    // C. Notification de succès avec mise à jour des mains
    
    // 1. Pour le Receveur (Client B - celui qui a accepté)
    // On récupère sa main mise à jour depuis la BDD
    char hand_recv[4096];
    db_get_player_cards_json(conn, id_receiver, hand_recv, sizeof(hand_recv));
    
    char msg_b[5000];
    // On envoie un objet JSON contenant à la fois un message et la liste "hand"
    snprintf(msg_b, sizeof(msg_b), 
             "{\"type\": \"response\", \"nom\": \"TradeResult\", \"status\": \"OK\", \"data\": {\"message\": \"Echange valide\", \"hand\": %s}}\n", 
             hand_recv);

    printf("[SERVEUR -> CLIENT %d] Envoi nouvelle main apres echange\n", client_socket);
    send(client_socket, msg_b, strlen(msg_b), 0); 

    // 2. Pour l'Initiateur (Client A - celui qui a proposé)
    if (socket_initiator != -1) {
        char hand_init[4096];
        db_get_player_cards_json(conn, id_initiator, hand_init, sizeof(hand_init));
        
        char msg_a[5000];
        snprintf(msg_a, sizeof(msg_a), 
                 "{\"type\": \"response\", \"nom\": \"TradeResult\", \"status\": \"OK\", \"data\": {\"message\": \"Offre acceptee\", \"hand\": %s}}\n", 
                 hand_init);
        
        printf("[SERVEUR -> CLIENT %d] Envoi nouvelle main apres echange\n", socket_initiator);
        send(socket_initiator, msg_a, strlen(msg_a), 0);
    }

    printf("[INFO] Echange termine avec succes.\n");
}

void process_fight_request(int client_socket, const char *json_payload) {
    char data_buffer[512];
    char temp_str[50];
    
    int id_initiator = -1;
    int id_card_initiator = -1;
    int id_receiver = -1;
    int id_card_receiver = -1;

    // 1. Extraction des données
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) {
        send_error_response(client_socket, ACTION_FIGHT, "Donnees manquantes");
        return;
    }

    if (extract_json_value(data_buffer, "id_initiator", temp_str, sizeof(temp_str))) id_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_initiator", temp_str, sizeof(temp_str))) id_card_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_receiver", temp_str, sizeof(temp_str))) id_receiver = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_receiver", temp_str, sizeof(temp_str))) id_card_receiver = atoi(temp_str);

    // Validation
    if (id_initiator == -1 || id_receiver == -1) {
        send_error_response(client_socket, ACTION_FIGHT, "IDs invalides");
        return;
    }

    // 2. Recherche du socket de l'adversaire
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

    // 3. Gestion Joueur absent
    if (socket_receiver == -1) {
        send_error_response(client_socket, ACTION_FIGHT, "L'adversaire n'est pas connecte");
        return;
    }

    // 4. Construction et envoi de la notification à l'adversaire
    char notification[1024];
    // Attention au \n final indispensable pour le client Java !
    snprintf(notification, sizeof(notification), 
             "{\"type\": \"request\", \"nom\": \"%s\", \"data\": {\"id_initiator\": %d, \"id_card_initiator\": %d, \"id_card_receiver\": %d}}\n",
             ACTION_FIGHT_CONFIRMATION, id_initiator, id_card_initiator, id_card_receiver);

    printf("[SERVEUR -> CLIENT %d (%s)] Transfert defi combat : %s", socket_receiver, receiver_username, notification);
    fflush(stdout);
    send(socket_receiver, notification, strlen(notification), 0);

    // 5. Ack à l'initiateur
    char ack[256];
    snprintf(ack, sizeof(ack), 
             "{\"type\": \"response\", \"nom\": \"%s\", \"status\": \"OK\", \"data\": \"Defi envoye a %s\"}\n", 
             ACTION_FIGHT, receiver_username);
    
    printf("[SERVEUR -> CLIENT %d] Ack combat : %s", client_socket, ack);
    send(client_socket, ack, strlen(ack), 0);
}

void process_fight_response(int client_socket, const char *json_payload) {
    char data_buffer[1024];
    char temp_str[50];
    int accepted = 0; // Par défaut false

    int id_initiator = -1, id_card_initiator = -1;
    int id_receiver = -1, id_card_receiver = -1;

    // 1. Parsing
    if (!extract_json_value(json_payload, "data", data_buffer, sizeof(data_buffer))) return;

    // Gestion du booléen "accepted" (votre parser gère true/false comme des chaines ou 1/0)
    // Adaptez selon ce que renvoie votre extract_json_value pour un booléen
    if (strstr(data_buffer, "\"accepted\": true") != NULL) accepted = 1;

    if (extract_json_value(data_buffer, "id_initiator", temp_str, sizeof(temp_str))) id_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_initiator", temp_str, sizeof(temp_str))) id_card_initiator = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_receiver", temp_str, sizeof(temp_str))) id_receiver = atoi(temp_str);
    if (extract_json_value(data_buffer, "id_card_receiver", temp_str, sizeof(temp_str))) id_card_receiver = atoi(temp_str);

    // 2. Retrouver le socket de l'initiateur (A)
    int socket_initiator = -1;
    pthread_mutex_lock(&clients_mutex);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients_list[i].logged_in && clients_list[i].client_id == id_initiator) {
            socket_initiator = clients_list[i].socket;
            break;
        }
    }
    pthread_mutex_unlock(&clients_mutex);

    // Si refusé, on notifie juste A et on s'arrête
    if (!accepted) {
        if (socket_initiator != -1) {
            char msg[] = "{\"type\": \"response\", \"nom\": \"FightResult\", \"status\": \"ERROR\", \"data\": \"Combat refuse\"}\n";
            send(socket_initiator, msg, strlen(msg), 0);
        }
        return;
    }

    // 3. EXECUTION DU COMBAT
    PGconn *conn = get_db_connection();

    char json_card_initiator[1024];
    char json_card_receiver[1024];

    db_get_single_card_json(conn, id_card_initiator, json_card_initiator, sizeof(json_card_initiator));
    db_get_single_card_json(conn, id_card_receiver, json_card_receiver, sizeof(json_card_receiver));    

    int atk1, def1, hp1, atk2, def2, hp2;

    // Récup stats
    if (db_get_card_stats(conn, id_card_initiator, &atk1, &def1, &hp1) != 0 ||
        db_get_card_stats(conn, id_card_receiver, &atk2, &def2, &hp2) != 0) {
        // Erreur (une carte n'existe plus ?)
        return; 
    }

    float dmg_to_2 = atk1 - (atk1 * (def2/100.0));
    float dmg_to_1 = atk2 - (atk2 * (def1/100.0));

    if (dmg_to_2 < 0) dmg_to_2 = 0; // Pas de soin par attaque
    if (dmg_to_1 < 0) dmg_to_1 = 0;

    // Application des dégâts
    int new_hp1 = hp1 - ((int) dmg_to_1);
    int new_hp2 = hp2 - ((int) dmg_to_2);

    // Mise à jour BDD (et suppression si mort)
    int status1 = db_update_card_hp(conn, id_card_initiator, new_hp1);
    int status2 = db_update_card_hp(conn, id_card_receiver, new_hp2);

    // 4. Enregistrement Blockchain
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

    // On détermine le gagnant (celui qui a fait le plus de dégats ou tué l'autre)
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

    // 5. Notification des résultats (Avec nouvelles mains)
    char hand_init[4096], hand_recv[4096];
    db_get_player_cards_json(conn, id_initiator, hand_init, sizeof(hand_init));
    db_get_player_cards_json(conn, id_receiver, hand_recv, sizeof(hand_recv));

    const char *res_receiver = (id_receiver == winner_id) ? "VICTOIRE" : "DEFAITE";
    const char *res_initiator = (id_initiator == winner_id) ? "VICTOIRE" : "DEFAITE";

    char result_msg[6000];
    
    // Message pour le Receveur (B)

    snprintf(result_msg, sizeof(result_msg), 
             "{\"type\": \"response\", \"nom\": \"FightResult\", \"status\": \"OK\", \"data\": {\"result\": \"%s\", \"log\": \"[%s] Combat termine. Tu as subi %d degats. Tu as inflige %d degats.\", \"hand\": %s, \"opponent_card\": %s}}\n", 
             res_receiver, res_receiver, ((int) dmg_to_2), ((int) dmg_to_1), hand_recv, json_card_initiator);
    send(client_socket, result_msg, strlen(result_msg), 0);

    // Message pour l'Initiateur (A)
    if (socket_initiator != -1) {
        snprintf(result_msg, sizeof(result_msg), 
             "{\"type\": \"response\", \"nom\": \"FightResult\", \"status\": \"OK\", \"data\": {\"result\": \"%s\", \"log\": \"[%s] Combat termine. Tu as subi %d degats. Tu as inflige %d degats.\", \"hand\": %s, \"opponent_card\": %s}}\n", 
             res_initiator, res_initiator, ((int) dmg_to_1), ((int) dmg_to_2), hand_init, json_card_receiver);
        send(socket_initiator, result_msg, strlen(result_msg), 0);
    }

    printf("[INFO] Combat termine : %d vs %d (Dmg: %d - %d). Gagnant : %d\n", id_initiator, id_receiver, ((int) dmg_to_1), ((int) dmg_to_2), winner_id);
}
#include "server.h"
#include <stdlib.h>
#include <stdio.h>

int main() {
    if (start_server() != 0) {
        perror("Le serveur n'a pas pu démarrer ou s'est arrêté avec une erreur");
        return EXIT_FAILURE;
    }
    return 0;
}
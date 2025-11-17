# Makefile

# Compilateur
CC = gcc

# Options
# -g: inclue les infos de debug
# -Wall: affiche tous les warnings et erreurs
# -Iinclude: indique le chemin vers les fichiers header 
# -fPIC : génère du code indépendant de la position
CFLAGS = -g -Wall -Iinclude -fPIC

# Bibliothèques
# -lpthread: bibliothèque des threads
# -lssl -lcrypto: bibliothèque pour la blockchain
# -lpq: bibliothèque pour PostgreSQL
LDLIBS = -lpthread -lssl -lcrypto -lpq

# Nom de l'exécutable
TARGET = server_exec

# Répertoires
SRCDIR = src
OBJDIR = build
BINDIR = bin

# Fichiers source .c
SOURCES = $(SRCDIR)/main.c $(SRCDIR)/server.c $(SRCDIR)/client_handler.c $(SRCDIR)/blockchain.c $(SRCDIR)/database.c

# Générer les noms de fichier .o
OBJECTS = $(patsubst $(SRCDIR)/%.c, $(OBJDIR)/%.o, $(SOURCES))

# Cible par défaut
all: $(BINDIR)/$(TARGET)

# Règle pour lier l'exécutable
$(BINDIR)/$(TARGET): $(OBJECTS)
		@mkdir -p $(BINDIR)
		$(CC) $(CFLAGS) -o $@ $^ $(LDLIBS)
		@echo "Compilation terminée. Exécutable : $@"

# Règle pour compiler les fichiers .c en .o
$(OBJDIR)/%.o: $(SRCDIR)/%.c
		@mkdir -p $(OBJDIR)
		$(CC) $(CFLAGS) -c -o $@ $<

# Cible pour nettoyer
clean:
		@echo "Nettoyage..."
		rm -f $(OBJDIR)/* $(BINDIR)/*

.PHONY: all clean 

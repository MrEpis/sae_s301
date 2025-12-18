# --- VARIABLES ---
JC = javac
JVM = java
SRC_DIR = src
BIN_DIR = bin
MAIN_CLASS = app.Main

# --- DÉTECTION JAVAFX ---
# 1. Valeur par défaut (Pour toi : cherche dans TON home)
DEFAULT_FX := $(shell find $(HOME) -name 'javafx.controls.jar' -print -quit 2>/dev/null)

# 2. Si l'utilisateur (le prof) définit JAVAFX_PATH en ligne de commande, on l'utilise.
# Sinon, on utilise le résultat de la recherche automatique.
ifndef JAVAFX_PATH
    ifneq ($(DEFAULT_FX),)
        JAVAFX_PATH := $(dir $(DEFAULT_FX))
    else
        JAVAFX_PATH := /usr/share/openjfx/lib
    endif
endif

# --- CONFIGURATION ---
JFX_FLAGS = --module-path $(JAVAFX_PATH) --add-modules javafx.controls,javafx.fxml
JFLAGS = -g -d $(BIN_DIR) -sourcepath $(SRC_DIR) $(JFX_FLAGS)

# --- RECETTES ---
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

default: compile

compile:
	@mkdir -p $(BIN_DIR)
	@echo "Config JavaFX utilisée : $(JAVAFX_PATH)"
	$(JC) $(JFLAGS) $(SOURCES)

run: compile
	@echo "Lancement..."
	$(JVM) $(JFX_FLAGS) -cp $(BIN_DIR) $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)
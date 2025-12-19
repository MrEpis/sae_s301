# --- VARIABLES ---
JC = javac
JVM = java
SRC_DIR = src
BIN_DIR = bin
MAIN_CLASS = app.Main

# Adresse par défaut si ADDR n'est pas précisé
ADDR ?= 134.59.27.129

# --- DÉTECTION JAVAFX ---
DEFAULT_FX := $(shell find $(HOME) -name 'javafx.controls.jar' -print -quit 2>/dev/null)

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
	@echo "Config JavaFX : $(JAVAFX_PATH)"
	$(JC) $(JFLAGS) $(SOURCES)

run: compile
	@echo "Lancement sur $(ADDR)..."
	$(JVM) $(JFX_FLAGS) -Dserver.addr=$(ADDR) -cp $(BIN_DIR) $(MAIN_CLASS)

run-bot: compile
	@echo "Lancement du bot sur $(ADDR)..."
	$(JVM) $(JFX_FLAGS) -Dserver.addr=$(ADDR) -cp $(BIN_DIR) $(MAIN_CLASS) bot

run-new: compile
	@echo "Lancement d'une instance vierge sur $(ADDR)..."
	$(JVM) $(JFX_FLAGS) -Dserver.addr=$(ADDR) -Dnosession=true -cp $(BIN_DIR) $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)
.PHONY: help desktop-dmg install-dmg

GRADLE := ./gradlew
DESKTOP_MODULE := :apps:desktopApp
DMG_DIR := core/apps/desktopApp/build/compose/binaries/main/dmg
INSTALL_SCRIPT := scripts/install-desktop-dmg.sh

help:
	@echo "Alvos disponíveis:"
	@echo "  desktop-dmg   Gera o instalador .dmg (Compose Desktop)"
	@echo "  install-dmg   Gera o .dmg e instala a app em /Applications (macOS)"

desktop-dmg:
	$(GRADLE) $(DESKTOP_MODULE):packageDmg

install-dmg: desktop-dmg
	@chmod +x $(INSTALL_SCRIPT)
	@$(INSTALL_SCRIPT)

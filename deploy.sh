#!/bin/bash

# PDF Merger Web - Deployment Script
# Dieses Script baut und startet die Anwendung mit Docker

set -e

echo "🚀 PDF Merger Web - Docker Deployment"
echo "======================================"
echo ""

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Prüfe ob Docker installiert ist
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker ist nicht installiert!${NC}"
    echo "Bitte installiere Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# Prüfe ob Docker läuft
if ! docker info &> /dev/null; then
    echo -e "${RED}❌ Docker läuft nicht!${NC}"
    echo "Bitte starte Docker Desktop."
    exit 1
fi

echo -e "${GREEN}✓ Docker ist installiert und läuft${NC}"
echo ""

# Menu
echo "Wähle eine Option:"
echo "1) Build und Start (docker-compose)"
echo "2) Nur Build (Docker Image erstellen)"
echo "3) Nur Start (Container starten)"
echo "4) Stop (Container stoppen)"
echo "5) Logs anzeigen"
echo "6) Status prüfen"
echo "7) Cleanup (Container und Images löschen)"
echo ""
read -p "Option (1-7): " option

case $option in
    1)
        echo -e "${YELLOW}📦 Building und Starting...${NC}"
        docker-compose up --build -d
        echo ""
        echo -e "${GREEN}✓ Fertig!${NC}"
        echo "🌐 PDF Merger läuft auf: http://localhost:8080"
        echo "📊 Logs anzeigen: docker-compose logs -f"
        echo "🛑 Stoppen: docker-compose down"
        ;;
    2)
        echo -e "${YELLOW}📦 Building Docker Image...${NC}"
        docker build -t pdf-merger-web:latest .
        echo ""
        echo -e "${GREEN}✓ Image erstellt!${NC}"
        echo "Starten mit: docker run -p 8080:8080 pdf-merger-web:latest"
        ;;
    3)
        echo -e "${YELLOW}🚀 Starting Container...${NC}"
        docker-compose up -d
        echo ""
        echo -e "${GREEN}✓ Container gestartet!${NC}"
        echo "🌐 PDF Merger läuft auf: http://localhost:8080"
        ;;
    4)
        echo -e "${YELLOW}🛑 Stopping Container...${NC}"
        docker-compose down
        echo -e "${GREEN}✓ Container gestoppt!${NC}"
        ;;
    5)
        echo -e "${YELLOW}📊 Logs (Ctrl+C zum Beenden):${NC}"
        docker-compose logs -f
        ;;
    6)
        echo -e "${YELLOW}📋 Container Status:${NC}"
        docker-compose ps
        echo ""
        echo -e "${YELLOW}📊 Resource Usage:${NC}"
        docker stats --no-stream pdf-merger-web
        ;;
    7)
        echo -e "${YELLOW}🧹 Cleanup...${NC}"
        docker-compose down
        docker rmi pdf-merger-web:latest 2>/dev/null || true
        docker system prune -f
        echo -e "${GREEN}✓ Cleanup abgeschlossen!${NC}"
        ;;
    *)
        echo -e "${RED}❌ Ungültige Option!${NC}"
        exit 1
        ;;
esac

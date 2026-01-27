# PDF Merger Web - Windows Deployment Script
# PowerShell Script für Windows-Benutzer

Write-Host "🚀 PDF Merger Web - Docker Deployment (Windows)" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Prüfe ob Docker installiert ist
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker ist nicht installiert!" -ForegroundColor Red
    Write-Host "Bitte installiere Docker Desktop: https://docs.docker.com/desktop/install/windows-install/"
    exit 1
}

# Prüfe ob Docker läuft
$dockerRunning = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker läuft nicht!" -ForegroundColor Red
    Write-Host "Bitte starte Docker Desktop."
    exit 1
}

Write-Host "✓ Docker ist installiert und läuft" -ForegroundColor Green
Write-Host ""

# Menu
Write-Host "Wähle eine Option:"
Write-Host "1) Build und Start (docker-compose)"
Write-Host "2) Nur Build (Docker Image erstellen)"
Write-Host "3) Nur Start (Container starten)"
Write-Host "4) Stop (Container stoppen)"
Write-Host "5) Logs anzeigen"
Write-Host "6) Status prüfen"
Write-Host "7) Cleanup (Container und Images löschen)"
Write-Host ""
$option = Read-Host "Option (1-7)"

switch ($option) {
    "1" {
        Write-Host "📦 Building und Starting..." -ForegroundColor Yellow
        docker-compose up --build -d
        Write-Host ""
        Write-Host "✓ Fertig!" -ForegroundColor Green
        Write-Host "🌐 PDF Merger läuft auf: http://localhost:8080"
        Write-Host "📊 Logs anzeigen: docker-compose logs -f"
        Write-Host "🛑 Stoppen: docker-compose down"
    }
    "2" {
        Write-Host "📦 Building Docker Image..." -ForegroundColor Yellow
        docker build -t pdf-merger-web:latest .
        Write-Host ""
        Write-Host "✓ Image erstellt!" -ForegroundColor Green
        Write-Host "Starten mit: docker run -p 8080:8080 pdf-merger-web:latest"
    }
    "3" {
        Write-Host "🚀 Starting Container..." -ForegroundColor Yellow
        docker-compose up -d
        Write-Host ""
        Write-Host "✓ Container gestartet!" -ForegroundColor Green
        Write-Host "🌐 PDF Merger läuft auf: http://localhost:8080"
    }
    "4" {
        Write-Host "🛑 Stopping Container..." -ForegroundColor Yellow
        docker-compose down
        Write-Host "✓ Container gestoppt!" -ForegroundColor Green
    }
    "5" {
        Write-Host "📊 Logs (Ctrl+C zum Beenden):" -ForegroundColor Yellow
        docker-compose logs -f
    }
    "6" {
        Write-Host "📋 Container Status:" -ForegroundColor Yellow
        docker-compose ps
        Write-Host ""
        Write-Host "📊 Resource Usage:" -ForegroundColor Yellow
        docker stats --no-stream pdf-merger-web
    }
    "7" {
        Write-Host "🧹 Cleanup..." -ForegroundColor Yellow
        docker-compose down
        docker rmi pdf-merger-web:latest 2>$null
        docker system prune -f
        Write-Host "✓ Cleanup abgeschlossen!" -ForegroundColor Green
    }
    default {
        Write-Host "❌ Ungültige Option!" -ForegroundColor Red
        exit 1
    }
}

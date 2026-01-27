# 🐳 Docker Deployment Guide

Komplette Anleitung zum Hosten der PDF Merger Web-App mit Docker.

## 📋 Voraussetzungen

- **Docker Desktop** installiert und gestartet
  - macOS: https://docs.docker.com/desktop/install/mac-install/
  - Windows: https://docs.docker.com/desktop/install/windows-install/
  - Linux: https://docs.docker.com/desktop/install/linux-install/

## 🚀 Schnellstart (Empfohlen)

### Option 1: Mit Deployment-Script (Einfachste Methode)

```bash
# Script ausführbar machen
chmod +x deploy.sh

# Interaktives Menu starten
./deploy.sh

# Wähle Option 1: "Build und Start"
```

Das Script führt dich durch alle Optionen! 🎉

### Option 2: Mit Docker Compose

```bash
# Build und Start in einem Befehl
docker-compose up --build -d

# Logs anzeigen
docker-compose logs -f

# Stoppen
docker-compose down
```

### Option 3: Mit Docker direkt

```bash
# Image bauen
docker build -t pdf-merger-web .

# Container starten
docker run -d \
  --name pdf-merger \
  -p 8080:8080 \
  pdf-merger-web

# Logs anzeigen
docker logs -f pdf-merger

# Stoppen
docker stop pdf-merger
docker rm pdf-merger
```

## 🌐 Zugriff

Nach dem Start ist die App erreichbar unter:
- **Lokal**: http://localhost:8080
- **Netzwerk**: http://YOUR_IP:8080

## 📊 Nützliche Commands

### Status prüfen
```bash
docker-compose ps
docker stats pdf-merger-web
```

### Logs anzeigen
```bash
# Alle Logs
docker-compose logs

# Live-Logs (folgen)
docker-compose logs -f

# Letzte 100 Zeilen
docker-compose logs --tail=100
```

### Container neu starten
```bash
docker-compose restart
```

### Health Check
```bash
curl http://localhost:8080/api/health
```

## 🔧 Konfiguration

### Port ändern

**In docker-compose.yml:**
```yaml
ports:
  - "3000:8080"  # Externes Port:Internes Port
```

**Oder mit Docker direkt:**
```bash
docker run -p 3000:8080 pdf-merger-web
```

### Umgebungsvariablen

**In docker-compose.yml:**
```yaml
environment:
  - SERVER_PORT=8080
  - SPRING_PROFILES_ACTIVE=production
  - LOGGING_LEVEL_ROOT=INFO
```

### Resource Limits anpassen

**In docker-compose.yml:**
```yaml
deploy:
  resources:
    limits:
      cpus: '4'      # Max 4 CPU Cores
      memory: 2G     # Max 2GB RAM
```

## 🌍 Produktions-Deployment

### Auf einem VPS/Server

1. **Dateien hochladen:**
```bash
scp -r pdf-merger-web user@your-server.com:/opt/
```

2. **Auf Server einloggen:**
```bash
ssh user@your-server.com
cd /opt/pdf-merger-web
```

3. **Mit Docker Compose starten:**
```bash
docker-compose up -d
```

4. **Nginx Reverse Proxy (Optional):**

Erstelle `/etc/nginx/sites-available/pdf-merger`:
```nginx
server {
    listen 80;
    server_name pdf.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # File upload limits
        client_max_body_size 200M;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/pdf-merger /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

5. **SSL mit Let's Encrypt (Optional):**
```bash
sudo certbot --nginx -d pdf.yourdomain.com
```

### Mit Cloud-Providern

#### **Railway.app** (Einfachste Option)
1. Gehe zu https://railway.app
2. "New Project" → "Deploy from GitHub repo"
3. Railway erkennt Dockerfile automatisch
4. Fertig! 🎉

#### **Heroku**
```bash
heroku login
heroku create pdf-merger-app
heroku container:push web
heroku container:release web
heroku open
```

#### **DigitalOcean App Platform**
1. Gehe zu DigitalOcean App Platform
2. "Create App" → GitHub Repository wählen
3. Dockerfile wird automatisch erkannt
4. Deploy! 🚀

#### **AWS ECS / Google Cloud Run / Azure Container Apps**
Alle unterstützen Docker Images direkt - folge deren Dokumentation.

## 🔒 Sicherheit

### Production Best Practices

1. **Umgebungsvariablen für Secrets:**
```yaml
environment:
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
```

2. **Read-only Filesystem (Optional):**
```yaml
read_only: true
tmpfs:
  - /tmp
```

3. **Non-root User:** ✅ Bereits im Dockerfile implementiert

4. **Health Checks:** ✅ Bereits konfiguriert

5. **Resource Limits:** ✅ Bereits gesetzt

## 🧹 Cleanup

### Alles entfernen
```bash
docker-compose down
docker rmi pdf-merger-web
docker system prune -a
```

### Nur Container stoppen
```bash
docker-compose down
```

## 🐛 Troubleshooting

### Port bereits belegt
```bash
# Ändere Port in docker-compose.yml
ports:
  - "8081:8080"
```

### Container startet nicht
```bash
# Logs prüfen
docker-compose logs

# Health Check manuell prüfen
docker exec pdf-merger-web wget -q -O- http://localhost:8080/api/health
```

### Zu wenig Speicher
```bash
# Docker Speicher erhöhen in Docker Desktop Settings
# Oder: Memory Limit in docker-compose.yml erhöhen
```

### Build schlägt fehl
```bash
# Cache leeren und neu bauen
docker-compose build --no-cache
```

## 📈 Monitoring

### Logs in Datei schreiben
```bash
docker-compose logs -f > logs.txt
```

### Prometheus Metrics (Advanced)
Füge zu `pom.xml` hinzu:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Metrics verfügbar unter: `http://localhost:8080/actuator/prometheus`

## 📞 Support

Bei Problemen:
1. Prüfe Logs: `docker-compose logs`
2. Prüfe Health: `curl http://localhost:8080/api/health`
3. Prüfe Docker Status: `docker ps`

---

**Happy Hosting! 🎉**

# PDF Merger Web 📄

Eine moderne, webbasierte Anwendung zum Zusammenführen von PDF-Dateien mit Drag & Drop Funktionalität.

## Features ✨

- 📤 **Drag & Drop Upload**: Ziehe PDFs direkt in den Browser
- 🔄 **Reordering**: Ändere die Reihenfolge der PDFs per Drag & Drop
- 🚀 **Spring Boot Backend**: Robustes Java Backend
- 🎨 **Modernes UI**: Responsives Design mit Gradient-Effekten
- 📊 **Progress Tracking**: Visuelles Feedback während der Verarbeitung
- 💾 **Direkter Download**: Sofortiger Download der zusammengeführten PDF

## Technologie-Stack 🛠️

- **Backend**: Spring Boot 3.2.1 + Java 17
- **PDF Processing**: Apache PDFBox 3.0.1
- **Frontend**: HTML5 + CSS3 + Vanilla JavaScript
- **Build Tool**: Maven

## Voraussetzungen 📋

- Java 17 oder höher
- Maven 3.6+ (oder IntelliJ IDEA mit integriertem Maven)

## Installation & Start 🚀

### Mit IntelliJ IDEA:

1. **Projekt öffnen**:
   - File → Open → Wähle den `pdf-merger-web` Ordner

2. **Maven Dependencies laden**:
   - IntelliJ lädt automatisch die Dependencies
   - Oder: Rechtsklick auf `pom.xml` → Maven → Reload Project

3. **Anwendung starten**:
   - Öffne `PdfMergerApplication.java`
   - Klicke auf den grünen "Run" Button
   - Oder: Rechtsklick → Run 'PdfMergerApplication'

4. **Browser öffnen**:
   - Gehe zu: `http://localhost:8080`

### Mit Maven (Kommandozeile):

```bash
# Dependencies installieren
mvn clean install

# Anwendung starten
mvn spring-boot:run
```

### JAR erstellen und ausführen:

```bash
# JAR erstellen
mvn clean package

# JAR ausführen
java -jar target/pdf-merger-web-1.0.0.jar
```

## Verwendung 📖

1. **PDFs hochladen**:
   - Klicke auf die Upload-Zone oder ziehe PDFs hinein
   - Mehrere Dateien werden unterstützt

2. **Reihenfolge ändern**:
   - Ziehe die PDF-Einträge per Drag & Drop
   - Die Nummerierung aktualisiert sich automatisch

3. **Dateinamen festlegen**:
   - Gib einen Namen für die Ausgabedatei ein
   - Standardname: `merged_output.pdf`

4. **Zusammenführen**:
   - Klicke auf "PDFs zusammenführen"
   - Die zusammengeführte PDF wird automatisch heruntergeladen

## Projektstruktur 📁

```
pdf-merger-web/
├── src/
│   └── main/
│       ├── java/com/pdfmerger/
│       │   ├── PdfMergerApplication.java    # Spring Boot Main
│       │   └── PdfMergerController.java     # REST API
│       └── resources/
│           ├── static/
│           │   └── index.html               # Frontend
│           └── application.properties       # Konfiguration
├── pom.xml                                  # Maven Dependencies
└── README.md
```

## API Endpoints 🔌

### POST `/api/merge`
Führt mehrere PDFs zusammen.

**Parameters**:
- `files`: MultipartFile[] - Array von PDF-Dateien
- `filename`: String (optional) - Name der Ausgabedatei

**Response**: 
- Content-Type: `application/pdf`
- Binary PDF data

### GET `/api/health`
Health Check Endpoint.

**Response**: 
```
PDF Merger Service is running!
```

## Konfiguration ⚙️

Die Konfiguration kann in `src/main/resources/application.properties` angepasst werden:

- **Port**: `server.port=8080`
- **Max File Size**: `spring.servlet.multipart.max-file-size=50MB`
- **Max Request Size**: `spring.servlet.multipart.max-request-size=200MB`

## Deployment 🌐

### Als JAR auf einem Server:

```bash
# JAR erstellen
mvn clean package

# Auf Server kopieren
scp target/pdf-merger-web-1.0.0.jar user@server:/path/

# Auf Server ausführen
java -jar pdf-merger-web-1.0.0.jar
```

### Mit Docker (optional):

Erstelle eine `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/pdf-merger-web-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t pdf-merger .
docker run -p 8080:8080 pdf-merger
```

## Troubleshooting 🔧

### Port bereits belegt:
```
Error: Port 8080 is already in use
```
Ändere den Port in `application.properties`:
```properties
server.port=8081
```

### OutOfMemoryError bei großen PDFs:
Erhöhe den Heap Space:
```bash
java -Xmx2g -jar target/pdf-merger-web-1.0.0.jar
```

### File Size Limit:
Passe die Limits in `application.properties` an:
```properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=500MB
```

## License 📄

MIT License - Frei verwendbar für private und kommerzielle Projekte.

## Support 💬

Bei Fragen oder Problemen öffne ein Issue im Repository.

---

**Viel Spaß beim Zusammenführen von PDFs! 🎉**

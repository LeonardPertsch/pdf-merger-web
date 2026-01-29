# ====================================
# RENDER-OPTIMIZED DOCKERFILE
# ====================================
# Multi-stage build für minimale Image-Größe

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Dependencies cachen
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Source code und build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime (minimalistisch!)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Wichtig: Temp-Verzeichnis für PDFBox
RUN mkdir -p /tmp/pdfbox && chmod 777 /tmp/pdfbox

# Non-root user (Security)
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /tmp/pdfbox
USER spring:spring

# JAR kopieren
COPY --from=build /app/target/pdf-merger-web-*.jar app.jar

EXPOSE 8080

# Health check (Render nutzt das)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# ====================================
# JVM TUNING FÜR RENDER FREE (512MB)
# ====================================
ENV JAVA_TOOL_OPTIONS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=70 \
  -XX:InitialRAMPercentage=50 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:+DisableExplicitGC \
  -Djava.io.tmpdir=/tmp/pdfbox \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.backgroundpreinitializer.ignore=true"

# Entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]

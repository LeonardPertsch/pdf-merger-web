# ====================================
# RENDER-OPTIMIZED DOCKERFILE
# ====================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install wget for health checks
RUN apk add --no-cache wget

# Create user
RUN addgroup -S spring && adduser -S spring -G spring

# CRITICAL: Create temp directories BEFORE switching to non-root user
RUN mkdir -p /tmp/pdfbox /tmp/tomcat && \
    chmod -R 777 /tmp && \
    chown -R spring:spring /tmp

# JAR kopieren
COPY --from=build /app/target/pdf-merger-web-*.jar app.jar
RUN chown spring:spring /app/app.jar

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

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

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
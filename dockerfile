# ============================================================
# STAGE 1: BUILD
# Gunakan Maven dengan JDK 21
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests


# ============================================================
# STAGE 2: RUNTIME
# Gunakan JRE 21 Minimal
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# Create non-root user, create uploads folder, and set permissions
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /app/uploads && \
    chown -R appuser:appgroup /app

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", \
  "app.jar"]
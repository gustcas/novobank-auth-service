# Etapa 1: Construcción
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY checkstyle.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S novobanco && adduser -S novobanco -G novobanco
COPY --from=builder /build/target/*.jar app.jar
USER novobanco
EXPOSE ${PORT:-8080}
ENV JAVA_OPTS=""
# ✅ Asegurar que la app escucha en el puerto correcto
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} $JAVA_OPTS -jar app.jar"]
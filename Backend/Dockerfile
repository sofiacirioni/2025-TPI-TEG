# Stage 1: Build con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom.xml primero para aprovechar cache de capas de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el resto del código y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime mínimo
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el jar compilado
# Jar: target/TEG-grupo8-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone Spring Boot
EXPOSE 8080

# Variables de entorno con defaults seguros
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

ENTRYPOINT ["java", "-jar", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "app.jar"]

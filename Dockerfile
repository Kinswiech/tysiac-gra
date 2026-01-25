# ETAP 1: Budowanie aplikacji (Maven z Javą 21)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Budujemy plik .jar, pomijając testy
RUN mvn clean package -DskipTests

# ETAP 2: Uruchamianie (Java 21)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Kopiujemy zbudowany plik z etapu 1
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
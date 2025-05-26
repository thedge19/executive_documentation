# Билд стадии
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /exec
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Финальная стадия
FROM eclipse-temurin:21-jre-jammy
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /app/pdfs && chmod 777 /app/pdfs
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

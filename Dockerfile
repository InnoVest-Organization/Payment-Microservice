# Dockerfile
FROM openjdk:23-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 5003
ENTRYPOINT ["java", "-jar", "app.jar"]


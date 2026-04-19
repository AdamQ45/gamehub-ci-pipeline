FROM openjdk:17-jdk-slim
COPY game-service-0.0.1-SNAPSHOT.jar /app/game-service.jar
ENTRYPOINT ["java", "-jar", "/app/game-service.jar"]

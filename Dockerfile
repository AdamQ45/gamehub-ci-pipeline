FROM eclipse-temurin:17-jre-alpine
COPY game-service-0.0.1-SNAPSHOT.jar /app/game-service.jar
ENTRYPOINT ["java", "-jar", "/app/game-service.jar"]

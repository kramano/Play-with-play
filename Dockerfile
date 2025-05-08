FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "app.jar"]
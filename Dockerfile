FROM openjdk:17-jdk-alpine AS build
WORKDIR /app
COPY . .
COPY serviceAccountKey.json /app/src/main/resources/serviceAccountKey.json
COPY application-prod.yml /app/src/main/resources/application-prod.yml
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

FROM openjdk:17
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]

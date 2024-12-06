FROM maven:3.8.6-eclipse-temurin-17 as build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean install


FROM openjdk:17-jdk-alpine as publish
ARG JAR_FILE=target/*.jar
COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
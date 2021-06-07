FROM maven:3.8.1-jdk-11-slim AS MAVEN_BUILD

# copy the pom and src code to the container
COPY ./ ./

# package our application code
RUN mvn clean package

FROM openjdk:11-jre

COPY --from=MAVEN_BUILD local-storage-service/server/target/local-storage-service-server.jar local-storage-service-server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "local-storage-service-server.jar"]
FROM maven:3.8.1-jdk-11-slim AS MAVEN_BUILD

# copy the pom and src code to the container
COPY ./ ./

# package our application code
RUN mvn clean package

FROM openjdk:11-jre

COPY --from=MAVEN_BUILD job-service/server/target/job-service-server.jar job-service-server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "job-service-server.jar"]
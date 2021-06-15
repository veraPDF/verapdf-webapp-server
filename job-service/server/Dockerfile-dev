FROM openjdk:11-jre
COPY target/job-service-server.jar job-service-server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "job-service-server.jar"]
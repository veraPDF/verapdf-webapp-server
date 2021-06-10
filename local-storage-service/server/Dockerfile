FROM openjdk:11-jre
COPY target/local-storage-service-server.jar local-storage-service-server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "local-storage-service-server.jar"]
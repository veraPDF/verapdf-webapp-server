FROM openjdk:11
COPY target/worker.jar worker.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "worker.jar"]

## Introduction and files overview

File `docker-compose.yml` contains configuration that uses images for services pulled from Docker Hub.

File `docker-compose-dev.yml` contains configuration that builds file storage service, job service and worker service from scratch.

File `docker-compose.override.yml` contains configuration necessary to run api-gateway module to achieve monolith-like behaviour of all other services.

File `.env` contains different security-sensitive settings.

## Run application with images pulled from Docker Hub
In order to start the application from Docker Hub images in the current directory run the following command to start docker compose:

`docker-compose up`

It will automatically pick up and use configurations from `docker-compose.yml` and `docker-compose.override.yml` with security-sensitive settings obtained from `.env` file.


Note: update `.env` file contents to define custom security-sensitive settings.

## Run application with local build
In order to build and run the application from scratch, e.g. during the development process, run the following command:

`docker-compose -f docker-compose-dev.yml -f docker-compose.override.yml up`

It will pick up and use configurations from `docker-compose-dev.yml` and `docker-compose.override.yml` with security-sensitive settings obtained from `.env` file.

Note: before running dev version of docker compose ensure that updated jar files have been built in project root directory using Maven:
`mvn clean package`
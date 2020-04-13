# Backend for veraPDF web application

## Build application

**Prerequisites**

JDK 11, Maven, Docker

**Build sources**

```
mvn clean install
```

## Running in Docker

**Run service stack**
```
cd ./docker
docker-compose up -d
```

**Available service endpoints**

To check service availability you can request service status endpoints described in [API Reference](https://github.com/veraPDF/verapdf-webapp-server/wiki/API-Reference#status)

**Stop service stack**
```
cd ./docker
docker-compose down
```

## Dev environment
When working on new features it is useful to be able to launch individual services in your IDE directly. 
To use your local services you can start the stack with `PROFILE=dev` environment variable:
```
cd ./docker
env PROFILE=dev docker-compose up -d
```
This will launch the nginx proxy with alternative configuration pointing to your services instead of those from 
containers.

Config assumes that you launch services on the following ports:

 * file storage service: `8090`
 * jobs service: `8091`
 
 ## API Reference
 https://github.com/veraPDF/verapdf-webapp-server/wiki/API-Reference

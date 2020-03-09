# verapdf-webapp-server
Back end server for the veraPDF web application

## How to build docker image
<i>Install Docker on your machine</i>
### Build image based on Dockerfile with name 'local_storage-service-server'
<code>docker build ./local-storage-service/server -t local-storage-service-server</code>
### Run image through container named 'local-storage-service-server-container' on port :8080
<code>docker run -p 8080:8080 --name local-storage-service-server-container --rm -d local-storage-service-server</code>
### To check the server service availability you need to make a request to specified URL
<i>localhost:8080/api/file/status/info</i>
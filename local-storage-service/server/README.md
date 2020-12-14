## What is file storage service?
File storage service manages files. It saves files on disk and provides file info and a link for downloading the file when necessary.

## Required dependencies
File storage service requires PostgreSQL database 8.2 or newer (tested on version 11.7) enabled.

## Environment Variables

`LOCALSTORAGE_DATABASE_HOST` (required) - Name of the host of running PostreSQL database used for file storage.

`LOCALSTORAGE_DATABASE_PORT` (optional, default: 5432) - exposed port for running PostreSQL database used for file storage.

`LOCALSTORAGE_DATABASE_DB` (optional, default: "postgres") - name of running PostreSQL database used for file storage.

`LOCALSTORAGE_DATABASE_USERNAME` (required) - username for running PostreSQL database used for file storage.

`LOCALSTORAGE_DATABASE_PASSWORD` (required) - password for running PostreSQL database used for file storage.

`LOCALSTORAGE_DISK_MIN_SPACE_THRESHOLD` (optional, default: 5GB) - the minimal amount of disk space required for accepting new files. Used to prevent disk overflow.

`LOCALSTORAGE_MAX_FILE_SIZE` (optional, default: 100MB) - maximum size of a single file, that a service can manage.

`LOCALSTORAGE_MAX_REQUEST_SIZE` (optional, default: 101MB) - maximum size of a single request, that a service can manage.

## REST API

For the details concerning API calls see [wiki](https://github.com/veraPDF/verapdf-webapp-server/wiki/API-Reference).
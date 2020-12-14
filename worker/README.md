## What is worker service?
Worker service executes validation tasks from the request queue and sends results of the validation to result queue, used further by verapdf/job-service. It uses verapdf/job-serivice to get the details of the job to be performed, such as validation profile type, and verapdf/file-storage to get the files for validation and to save the result of the validation process.

## Required dependencies
Worker service requires RabbitMQ message-broker service and running instances of verapdf/file-storage and verapdf/job-service.

## Environment variables

`AMQP_SERVER_HOST` (required) - name of the host of running RabbitMQ message-broker used for exchanging messages between worker service and verapdf/job-service.

`AMQP_USER` (required) - username for running RabbitMQ message-broker used for exchanging messages between worker service and verapdf/job-service.

`AMQP_PASSWORD ` (required) - password for running RabbitMQ message-broker used for exchanging messages between worker service and verapdf/job-service

`AMQP_SERVER_CONCURRENCY` (optional, default: 4) -  integer default number of consumers that concurrently process messages.

`AMQP_SERVER_MAX_CONCURRENCY` (optional, default: 8) - integer number of maximum amount of consumers that can concurrently process messages.

`AMQP_SERVER_SENDING_QUEUE_NAME` (required) - name of the queue in RabbitMQ message-broker, where the validation results will be sent to. Should be the same as the *AMQP_SERVER_LISTENING_QUEUE_NAME* in verapdf/job-service configuration.

`AMQP_SERVER_SENDING_QUEUE_MAX_SIZE` (optional, default: 2047MB) - max size of the queue containing validation results to be processed.

`AMQP_SERVER_LISTENING_QUEUE_NAME` (required) - name of the queue in RabbitMQ message-broker, where the validation task will be fetched from. Should be the same as the *AMQP_SERVER_SENDING_QUEUE_NAME* in verapdf/job-service configuration.

`AMQP_SERVER_LISTENING_QUEUE_MAX_SIZE` (optional, default: 2047MB) - max size of the queue containing validation tasks to be processed.

`JOB_SERVICE_HOST` (required) - the name of the host for verapdf/job-service instance.

`JOB_SERVICE_PORT` (required) - the port of the host for verapdf/job-service instance.

`FILE_STORAGE_HOST` (required) - the name of the host for verapdf/file-storage instance.

`FILE_STORAGE_PORT` (required) - the port of the host for verapdf/file-storage instance.

## REST API

For the details concerning API calls see [wiki](https://github.com/veraPDF/verapdf-webapp-server/wiki/API-Reference).
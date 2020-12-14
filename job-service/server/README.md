## What is job service?
Job service manages jobs, that contain info about operations necessary for files validation. It uses job info to create validation tasks and sends these tasks to a queue for verapdf/worker service to pick up. It also  stores the info about validation result file

## Required dependencies
Job service requires PostgreSQL database 8.2 or newer (tested on version 11.7) and RabbitMQ message-broker service.

## Environmental variables

`JOBSERVICE_DATABASE_HOST` (required) - name of the host of running PostreSQL database used for job storage.

`JOBSERVICE_DATABASE_PORT`  (optional, default: 5432) - exposed port for running PostreSQL database used for job storage.

`JOBSERVICE_DATABASE_DB`  (optional, default: "job_postgres") - name of running PostreSQL database used for job storage.

`JOBSERVICE_DATABASE_USERNAME` (required) - username for running PostreSQL database used for job storage.

`JOBSERVICE_DATABASE_PASSWORD` (required) - password for running PostreSQL database used for job storage.

`AMQP_SERVER_HOST` (required) - name of the host of running RabbitMQ message-broker used for exchanging messages between job service and verapdf/worker service.

`AMQP_USER` (required) - username for running RabbitMQ message-broker used for exchanging messages between job service and verapdf/worker service.

`AMQP_PASSWORD` (required) - password for running RabbitMQ message-broker used for exchanging messages between job service and verapdf/worker service.

`AMQP_SERVER_CONCURRENCY` (optional, default: 4) - integer default number of consumers that concurrently process messages.

`AMQP_SERVER_MAX_CONCURRENCY` (optional, default: 8) - integer number of maximum amount of consumers that can concurrently process messages.

`AMQP_SERVER_SENDING_QUEUE_NAME` (required) - name of the queue in RabbitMQ message-broker, where the validation task will be sent to. Should be the same as the *AMQP_SERVER_LISTENING_QUEUE_NAME* in verapdf/worker configuration.

`AMQP_SERVER_SENDING_QUEUE_MAX_SIZE` (optional, default: 2047MB) - max size of the queue containing validation tasks to be processed.

`AMQP_SERVER_LISTENING_QUEUE_NAME` (required) - name of the queue in RabbitMQ message-broker, where the validation results will be fetched from. Should be the same as the *AMQP_SERVER_SENDING_QUEUE_NAME* in verapdf/worker configuration.

`AMQP_SERVER_LISTENING_QUEUE_MAX_SIZE`(optional, default: 2047MB) -  max size of the queue containing validation results to be processed.

## REST API

For the details concerning API calls see [wiki](https://github.com/veraPDF/verapdf-webapp-server/wiki/API-Reference).
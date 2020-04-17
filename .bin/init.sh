#!/usr/bin/env bash
# Create a volume to reuse downloaded maven dependencies
docker volume create --name maven-repo

#!/usr/bin/env bash

# Workaround for running from Git Bash on Windows:
export MSYS_NO_PATHCONV=1

script_path=$(realpath "$0")
script_dir=$(dirname "$script_path")
project_root=$(dirname "$script_dir")

work_dir="/usr/src/verapdf-webapp-server"

# Build all project sources
docker run -it \
  --rm --name build-verapdf-webapp-server \
  -v maven-repo:/root/.m2 \
  -v "$project_root":$work_dir \
  -w $work_dir \
  maven:3.6-jdk-11 mvn clean package

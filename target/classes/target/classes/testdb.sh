#!/bin/bash

TESTING_DB_DIR=/tmp/rest-easy-test

## A script to create a new empty database for testing rest-easy

# Purge the previous data, if it exists
rm -rf $TESTING_DB_DIR

# Re-create database 
initdb $TESTING_DB_DIR
(
  sleep 5;
  createdb -h ::1 app &&
  echo "DB created"
)&

# Start Postgres
postgres -c unix_socket_directories=/tmp -D $TESTING_DB_DIR



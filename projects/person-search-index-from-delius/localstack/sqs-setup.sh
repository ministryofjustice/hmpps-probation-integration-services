#!/usr/bin/env bash
echo creating queues...
awslocal sqs create-queue --queue-name person-queue
awslocal sqs create-queue --queue-name person-dlq
awslocal sqs create-queue --queue-name contact-queue
awslocal sqs create-queue --queue-name contact-dlq

# Testing:
# awslocal sqs send-message '{"Message": "{\"offenderId\": 1}"}'
# awslocal sqs send-message '{"Message": "{\"sourceId\": 1}"}'
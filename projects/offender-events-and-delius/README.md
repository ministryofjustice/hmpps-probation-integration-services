# offender-events-and-delius

Outbound Service sending offender events based on changes in National Delius.

## Dev Instructions

By default, offender-events will use the ActiveMQ (JMS) queue to send messages for development. 
This means just starting the application using the dev class path is sufficient to run the database (H2) and outbound message queue.

### Using LocalStack

If desired, the app can be connected to a localstack instance to reflect the AWS SNS topic.

Use the docker-compose file provided with the project to start up localstack services in a container

```shell
docker-compose up -d
```

For the following connect to the shell of the container 
```shell
docker exec -it localstack /bin/bash
```

It will be necessary to configure the aws cli before use. The values are not validated so any access key id or access secret can be used.

```shell
aws configure
```

#### Create the topic: 

```shell
aws --endpoint-url=http://localhost:4566 sns create-topic --name offender-events --region eu-west-2
```

#### Create a queue:

```shell
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name offender-events --region eu-west-2
```

#### Subscribe the queue to the topic to receive messages published to the topic:

```shell
aws --endpoint-url=http://localhost:4566 sns subscribe \
--topic-arn arn:aws:sns:eu-west-2:000000000000:offender-events \
--protocol sqs \
--notification-endpoint http://localhost:4566/000000000000/offender-events
```

#### Check for messages

```shell
aws --endpoint-url=http://localhost:4566 sqs receive-message \
--queue-url http://localhost:4566/000000000000/offender-events \
--max-number-of-messages 10
```

Add the following environment variables before running the application:

```shell
MESSAGING_PRODUCER_TOPIC=arn:aws:sns:eu-west-2:000000000000:offender-events
CLOUD_AWS_SQS_REGION=eu-west-2 # for localstack only - not required in AWS
CLOUD_AWS_SQS_ENDPOINT=http://localhost:4566 # for localstack only - not required in AWS
```
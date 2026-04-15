import boto3
import json
import sys

sqs = boto3.client("sqs", region_name="eu-west-2")
sns = boto3.client("sns", region_name="eu-west-2")


def read_sqs_messages(queue_url):
    """
    Reads then deletes all messages from an SQS queue, and prints the
    JSON to stdout line-by-line.

    Example usage for reading messages to a file:
     NAMESPACE=... SECRET_NAME=... source aws-creds-from-k8s.sh && \
     python3 sqs-utils.py read "$SQS_QUEUE_URL" | tee sqs-messages.log

    :param queue_url: The URL of the SQS queue.
    :return:
    """
    response = sqs.receive_message(QueueUrl=queue_url, MaxNumberOfMessages=10)

    count = 0
    while "Messages" in response:
        for message in response["Messages"]:
            print(json.dumps(json.loads(message["Body"])))
            sqs.delete_message(QueueUrl=queue_url,
                               ReceiptHandle=message["ReceiptHandle"])
            count += 1

        response = sqs.receive_message(
            QueueUrl=queue_url, MaxNumberOfMessages=10)

    print(f"Total messages: {count}", file=sys.stderr)


def send_sqs_messages(queue_url):
    """
    Send messages to the specified SQS queue. Reads each line from stdin,
    parses it as a single JSON message, and sends it to the queue.

    Example usage for sending messages from a file:

     NAMESPACE=... SECRET_NAME=... source aws-creds-from-k8s.sh && \
     cat sqs-messages.log | python3 sqs-utils.py send "$SQS_QUEUE_URL" | tee send-output.log

    :param queue_url: The URL of the SQS queue.
    :return:
    """
    success = 0
    failure = 0
    for line in sys.stdin:
        message = json.loads(line)
        message_attributes = dict((k, {"StringValue": v["Value"], "DataType": "String"})
                                  for k, v in message["MessageAttributes"].items())
        response = sqs.send_message(MessageBody=json.dumps(message),
                                    MessageAttributes=message_attributes,
                                    QueueUrl=queue_url)
        print(response)
        if response["ResponseMetadata"]["HTTPStatusCode"] == 200:
            success += 1
        else:
            failure += 1

    print(f"Successfully sent {success} messages. Failures={failure}")


def publish_sns_notifications(topic_arn):
    """
    Publishes notifications to the specified SNS topic. Reads each line from stdin,
    parses it as a single JSON message, and sends it to the topic.

    Example usage for sending messages from a file:

     cat notifications.jsonl | python3 sqs-utils.py publish "$SNS_TOPIC_ARN" | tee send-output.log

    :param topic_arn: The ARN of the SNS topic.
    :return:
    """
    success = 0
    for line in sys.stdin:
        notification = json.loads(line)
        message = notification['Message']
        message_attributes = dict((k, {"StringValue": v["Value"], "DataType": "String"})
                                  for k, v in notification["MessageAttributes"].items())
        response = sns.publish(Message=message,
                               MessageAttributes=message_attributes,
                               TopicArn=topic_arn)
        print(response)
        success += 1

    print(f"Successfully published {success} notifications.")


def get_approximate_number_of_messages(queue_url):
    """
    Gets the approximate number of messages in the specified SQS queue.

    :param queue_url: The URL of the SQS queue.
    :return:
    """
    response = sqs.get_queue_attributes(AttributeNames=["ApproximateNumberOfMessages"],
                                        QueueUrl=queue_url)
    print(response["Attributes"]["ApproximateNumberOfMessages"])


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print(
            f"Usage: {sys.argv[0]} <count|read|send> <sqs_queue_url>", file=sys.stderr)
        sys.exit(1)

    if sys.argv[1] == "count":
        get_approximate_number_of_messages(sys.argv[2])
    elif sys.argv[1] == "read":
        read_sqs_messages(sys.argv[2])
    elif sys.argv[1] == "send":
        send_sqs_messages(sys.argv[2])
    elif sys.argv[1] == "publish":
        publish_sns_notifications(sys.argv[2])

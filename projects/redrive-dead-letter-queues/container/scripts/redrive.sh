#!/bin/bash
set -euo pipefail
eval "$(sentry-cli bash-hook --no-environ)"

queue_urls=$(aws sqs list-queues --queue-name-prefix "probation-integration-$ENVIRONMENT" | jq -r '.QueueUrls[] | select(endswith("-dlq"))')

for queue_url in $queue_urls
do
  echo "Redriving $queue_url"
  queue_arn=$(aws sqs get-queue-attributes --attribute-names QueueArn --queue-url "$queue_url" --query Attributes.QueueArn --output text)
  aws sqs start-message-move-task --source-arn "$queue_arn"
done
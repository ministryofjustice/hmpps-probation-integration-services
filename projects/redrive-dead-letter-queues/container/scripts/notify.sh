#!/usr/bin/env bash
set -euo pipefail
eval "$(sentry-cli bash-hook --no-environ)"

queue_urls=$(aws sqs list-queues --queue-name-prefix "$QUEUE_NAME_PREFIX" | jq -r '.QueueUrls[] | select(endswith("-dlq"))')

queue_stats_json='{}' # Example: {"dev": {"queue1": 1, "queue2": 2, "queue3": 3}, "preprod": {"queue1": 1}}
for queue_url in $queue_urls; do
  count=$(aws sqs get-queue-attributes --queue-url "$queue_url" --attribute-names ApproximateNumberOfMessages --query 'Attributes.ApproximateNumberOfMessages' --output text)
  queue_name=$(basename "$queue_url" | sed "s/$QUEUE_NAME_PREFIX-//;s/-dlq//")
  environment_name=$(echo "$queue_name" | cut -d'-' -f1)
  service_name=$(echo "$queue_name" | cut -d'-' -f2-)

  if [ "$count" -ne 0 ]; then
    echo "$service_name has $count messages"
    queue_stats_json=$(jq --arg key "$environment_name" --arg key2 "$service_name" --arg value "$count" '.[$key][$key2] = ($value | tonumber)' <<< "$queue_stats_json")
  fi
done

if [ "$queue_stats_json" = '{}' ]; then
  echo "Dead-letter queues are empty!"
  exit 0
fi

echo "$queue_stats_json"
echo "Posting dead-letter queue stats to Slack..."
curl -H "Authorization: Bearer $SLACK_TOKEN" --json "$(echo "$queue_stats_json" | jq -rc --arg SLACK_CHANNEL "$SLACK_CHANNEL" '. | {
  "channel": $SLACK_CHANNEL,
  "unfurl_links": false,
  "unfurl_media": false,
  "blocks": ([
    {
      "type": "header",
      "text": {
        "type": "plain_text",
        "text": "🚦 Dead-letter queue report"
      }
    },
    {
      "type": "divider"
    },
    {
      "type": "context",
      "elements": [
        {
          "type": "mrkdwn",
          "text": "There are un-processed messages on the dead-letter queues. Review the messages in the <https://justice-cloud-platform.eu.auth0.com/samlp/mQev56oEa7mrRCKAZRxSnDSoYt6Y7r5m?connection=github|AWS Console> and check <https://ministryofjustice.sentry.io/issues/?statsPeriod=14d|Sentry> for issues."
        }
      ]
    }
  ]
  +
  (. | to_entries | map(
    {
      "type": "section",
      "text": {
        "type": "mrkdwn",
        "text": ("*" + .key + "*")
      }
    },
    {
      "type": "divider"
    },
    (.key as $environment_name | .value | to_entries | map(
      [
        {
          "type": "section",
          "text": {
            "text": (">" + .key),
            "type": "mrkdwn"
          },
          "accessory": {
            "type": "button",
            "text": {
              "type": "plain_text",
              "text": (.value | tostring)
            },
            "url": ("https://eu-west-2.console.aws.amazon.com/sqs/v3/home?region=eu-west-2#/queues/https%3A%2F%2Fsqs.eu-west-2.amazonaws.com%2F754256621582%2Fprobation-integration-" + $environment_name + "-" + .key + "-dlq")
          }
        }
      ]
    ) | flatten)[]
  ))
  +
  [
    {
      "type": "divider"
    },
    {
      "type": "actions",
      "elements": [
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": ":aws:  AWS Console"
          },
          "url": "https://justice-cloud-platform.eu.auth0.com/samlp/mQev56oEa7mrRCKAZRxSnDSoYt6Y7r5m?connection=github"
        },
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": ":prometheus: Prometheus"
          },
          "url": "https://prometheus.live.cloud-platform.service.justice.gov.uk/graph?g0.expr=(sum%20by%20(queue_name)%20(aws_sqs_approximate_number_of_messages_visible_maximum%7Bqueue_name%3D~%22.*probation-integration-.*-dlq%22%7D%20offset%205m)%20%3E%200)&g0.tab=0&g0.display_mode=lines&g0.show_exemplars=0&g0.range_input=1d"
        },
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": ":sentry: Sentry"
          },
          "url": "https://ministryofjustice.sentry.io/issues/?statsPeriod=14d"
        }
      ]
    }
  ])
 }')" https://slack.com/api/chat.postMessage
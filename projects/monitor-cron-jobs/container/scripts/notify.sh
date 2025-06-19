#!/usr/bin/env bash
set -euo pipefail

get_timeout_for_job() {
  local job_name="$1"
  case "$job_name" in
    person-reindex-*)           echo 7200   ;; # 3 hours
    contact-keyword-reindex-*)  echo 259200 ;; # 3 days
    contact-semantic-reindex-*) echo 259200 ;; # 3 days
    *)                          echo 900    ;; # 15 minutes (default)
  esac
}

# Fetch all jobs from the last 24 hours
job_data=$(kubectl get jobs -o json | jq '
  [
    .items[]
    | select(.status.completionTime == null or (.status.startTime | fromdateiso8601) > (now - 86400))
    | {
        name: .metadata.name,
        status:
          (if (.status.succeeded // 0) > 0 then "Completed"
          elif (.status.failed // 0) > 0 then "Failed"
          else "Running" end),
        duration_seconds: (
          ((.status.completionTime // (now | todate)) | fromdateiso8601) - (.status.startTime | fromdateiso8601)
        )
      }
  ]
')
echo "$job_data"

failed_blocks="[]"
running_blocks="[]"
completed_blocks="[]"
for job_json in $(echo "$job_data" | jq -c '.[]'); do
  name=$(echo "$job_json" | jq -r .name)
  status=$(echo "$job_json" | jq -r .status)
  duration_seconds=$(echo "$job_json" | jq -r .duration_seconds | cut -d'.' -f1)
  duration_friendly=$(kubectl get job "$name" --no-headers 2>/dev/null | awk '{print $4}' || echo "N/A")
  # shellcheck disable=SC2016
  job_block='. + [{
    type: "section",
    text: { type: "mrkdwn", text: (">" + $name) },
    accessory: {
      type: "button",
      text: { type: "plain_text", text: $text },
      url: ("https://app-logs.cloud-platform.service.justice.gov.uk/_dashboards/app/data-explorer/discover#?_a=(discover:(columns:!(log),sort:!()),metadata:(view:discover))&_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-1d,to:now))&_q=(filters:!((meta:(),query:(match_phrase:(kubernetes.namespace_name:hmpps-probation-integration-services-" + $env + "))),(meta:(),query:(match_phrase:(kubernetes.labels.batch_kubernetes_io%2Fjob-name:" + $name + ")))),query:())")
    }
  }]'
  case "$status" in
    "Failed")
      text='❌'
      failed_blocks=$(jq --arg name "$name" --arg text "$text" --arg env "$ENVIRONMENT_NAME" "$job_block" <<<"$failed_blocks")
      ;;

    "Running")
      if (( duration_seconds > $(get_timeout_for_job "$name") )); then text="⚠️ ${duration_friendly}"; else text="⏳ ${duration_friendly}"; fi
      running_blocks=$(jq --arg name "$name" --arg text "$text" --arg env "$ENVIRONMENT_NAME" "$job_block" <<<"$running_blocks")
      ;;

    "Completed")
      text="✅ ${duration_friendly}"
      completed_blocks=$(jq --arg name "$name" --arg text "$text" --arg env "$ENVIRONMENT_NAME" "$job_block" <<<"$completed_blocks")
      ;;
  esac
done

if [[ $(echo "$failed_blocks" | jq 'length') -eq 0 && $(echo "$running_blocks" | jq 'length') -eq 0 ]]; then
  echo "No failing or long-running cron jobs found!"
  exit 0
fi

payload=$(jq -n \
  --arg SLACK_CHANNEL "$SLACK_CHANNEL" \
  --arg ENVIRONMENT_NAME "$ENVIRONMENT_NAME" \
  --argjson failed_blocks "$failed_blocks" \
  --argjson running_blocks "$running_blocks" \
  --argjson completed_blocks "$completed_blocks" \
  '
  {
    channel: $SLACK_CHANNEL,
    unfurl_links: false,
    unfurl_media: false,
    blocks: ([
      {
        type: "header",
        text: { type: "plain_text", text: ("🗓️ Cron jobs (" + $ENVIRONMENT_NAME + ")") }
      },
      { type: "divider" },
      {
        type: "context",
        elements: [
          {
            type: "mrkdwn",
            text: ("Scheduled jobs have failed or have been running for longer than expected in " + $ENVIRONMENT_NAME + ".\n\n<https://app-logs.cloud-platform.service.justice.gov.uk/_dashboards/app/data-explorer/discover/#/view/b9c6db70-4c4f-11f0-96ce-cdfb3473006e?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-1d,to:now))|Review the logs> and delete any failed jobs to clear this alert.")
          }
        ]
      }
    ]
    + (if $failed_blocks | length > 0 then
        ([
          { type: "section", text: { type: "mrkdwn", text: "*Failed*" }},
          { type: "divider" }
        ]) + $failed_blocks
      else [] end)
    + (if $running_blocks | length > 0 then
        [
          { type: "section", text: { type: "mrkdwn", text: "*Running*" }},
          { type: "divider" }
        ] + $running_blocks
      else [] end)
    + (if $completed_blocks | length > 0 then
        [
          { type: "section", text: { type: "mrkdwn", text: "*Completed*" }},
          { type: "divider" }
        ] + $completed_blocks
      else [] end)
    + [
      { type: "divider" },
      {
        type: "actions",
        elements: [
          {
            type: "button",
            text: { type: "plain_text", text: "📋  Logs" },
            url: "https://app-logs.cloud-platform.service.justice.gov.uk/_dashboards/app/data-explorer/discover/#/view/b9c6db70-4c4f-11f0-96ce-cdfb3473006e?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-1d,to:now))"
          },
          {
            type: "button",
            text: { type: "plain_text", text: ":sentry: Sentry" },
            url: "https://ministryofjustice.sentry.io/issues/?statsPeriod=1d"
          }
        ]
      }
    ])
  }
')

echo "Posting cron job status to Slack..."
curl --fail --show-error -H "Authorization: Bearer $SLACK_TOKEN" --json "$payload" https://slack.com/api/chat.postMessage

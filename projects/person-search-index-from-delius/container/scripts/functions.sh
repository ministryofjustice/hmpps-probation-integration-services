#!/bin/bash
set -eo pipefail
eval "$(sentry-cli bash-hook --no-environ)"

function fail() {
  message=$1
  exception=$2
  echo "$message" >&2
  if [ -n "$exception" ]; then track_exception "$exception" "$message"; fi
  exit 1
}

function curl_json() {
  curl --fail --silent --show-error --header 'Content-Type: application/json' "$@" && echo
}

function parse_connection_string() {
  terms=$(echo "$APPLICATIONINSIGHTS_CONNECTION_STRING" | tr ";" "\n")
  for term in $terms; do
      key=$(echo "$term" | cut -d "=" -f 1)
      value=$(echo "$term" | cut -d "=" -f 2)
      if [ "$key" = 'InstrumentationKey' ]; then APP_INSIGHTS_KEY="$value"; fi
      if [ "$key" = 'IngestionEndpoint' ]; then APP_INSIGHTS_URL="${value}v2/track"; fi
  done
  echo "APP_INSIGHTS_URL=$APP_INSIGHTS_URL"
}

function track_custom_event() {
  echo "Sending custom event to app insights: $1 ${2:-"{}"}"
  if [ -z "$APPLICATIONINSIGHTS_CONNECTION_STRING" ]; then echo 'Missing APPLICATIONINSIGHTS_CONNECTION_STRING. Telemetry is disabled.'; return; fi
  if [ -z "$APP_INSIGHTS_URL" ]; then parse_connection_string; fi
  now=$(date +'%FT%T%z')
  curl_json -XPOST "$APP_INSIGHTS_URL" --data '{
    "name": "'"$1"'",
    "time": "'"$now"'",
    "iKey": "'"$APP_INSIGHTS_KEY"'",
    "tags": {
      "ai.cloud.role": "person-search-index-from-delius"
    },
    "data": {
      "baseType": "EventData",
      "baseData": {
        "ver": 1,
        "name": "'"$1"'",
        "properties": '"${2:-"{}"}"'
      }
    }
  }'
}

function track_exception() {
  echo "Sending exception to app insights: $1 $2"
  if [ -z "$APPLICATIONINSIGHTS_CONNECTION_STRING" ]; then echo 'Missing APPLICATIONINSIGHTS_CONNECTION_STRING. Telemetry is disabled.'; return; fi
  if [ -z "$APP_INSIGHTS_URL" ]; then parse_connection_string; fi
  now=$(date +'%FT%T%z')
  curl_json -XPOST "$APP_INSIGHTS_URL" --data '{
    "name": "'"$1"'",
    "time": "'"$now"'",
    "iKey": "'"$APP_INSIGHTS_KEY"'",
    "tags": {
      "ai.cloud.role": "person-search-index-from-delius"
    },
    "data": {
      "baseType": "ExceptionData",
      "baseData": {
        "ver": 1,
        "handledAt": "'"$1"'",
        "properties": {},
        "exceptions": [
          {
            "typeName": "'"$1"'",
            "message": "'"$2"'",
            "hasFullStack": false,
            "parsedStack": []
          }
        ]
      }
    }
  }'
}

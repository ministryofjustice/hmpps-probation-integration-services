#!/bin/bash
set -eo pipefail

function help {
  echo -e "\\nSCRIPT USAGE\\n"
  echo "-i The prefix of the primary and standby index name (e.g. 'person-search')"
  echo "-t The time in seconds to wait before attempting to switch aliases2"
  echo "-u Search Host URL"
  exit 1
}

export SEARCH_URL="${SEARCH_INDEX_HOST}"
export TIMEOUT=7200

while getopts h:i:t:u: FLAG; do
  case $FLAG in
  h) help ;;
  i) export INDEX_PREFIX="$OPTARG" ;;
  t) export TIMEOUT="$OPTARG" ;;
  u) export SEARCH_URL="$OPTARG" ;;
  \?) #unrecognized option - show help
    echo -e \\n"Option not allowed."
    help
    ;;
  esac
done
if [ -z "$INDEX_PREFIX" ]; then echo 'Missing -i' >&2; exit 1; fi

function curl_json() {
  curl --fail --silent --show-error --header 'Content-Type: application/json' "$@"
  echo
}

function get_current_indices() {
  PRIMARY_INDEX=$(curl_json --retry 3 "${SEARCH_URL}/_alias/${INDEX_PREFIX}-primary" | jq -r 'keys[0]')
  STANDBY_INDEX=$(curl_json --retry 3 "${SEARCH_URL}/_alias/${INDEX_PREFIX}-standby" | jq -r 'keys[0]')
  echo "Search URL: ${SEARCH_URL}"
  echo "Primary Index => ${PRIMARY_INDEX}"
  echo "Standby Index => ${STANDBY_INDEX}"

  if [ -z "$PRIMARY_INDEX" ] || [ -z "$STANDBY_INDEX" ] || [ "$PRIMARY_INDEX" = 'error' ] || [ "$STANDBY_INDEX" = 'error' ]; then
    echo "Unable to get Index Aliases."
    exit 1
  fi
}

delete_ready_for_reindex() {
  echo "deleting ${STANDBY_INDEX} ready for indexing ..."
  curl_json -XDELETE "${SEARCH_URL}/${STANDBY_INDEX}"
  curl_json -XPUT "${SEARCH_URL}/${STANDBY_INDEX}" --data '{"aliases": {"'"${INDEX_PREFIX}-primary"'": {}}}'
}

parseAppInsightsConnectionString() {
    terms=$(echo "${APPLICATIONINSIGHTS_CONNECTION_STRING}" | tr ";" "\n")

    for term in $terms
    do
        key=$(echo "$term" | cut -d "=" -f 1)
        value=$(echo "$term" | cut -d "=" -f 2)
        if [ "$key" = 'InstrumentationKey' ];
          then APP_INSIGHTS_KEY="$value"
        fi
        if [ "$key" = 'IngestionEndpoint' ];
          then APP_INSIGHTS_URL="$value"v2/track
        fi
    done

    printf "\\nAPP INSIGHTS URL:  %s\\n" "$APP_INSIGHTS_URL"
}

check_count_document() {
  EXPECTED_COUNT=$(curl_json "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1" | jq '._source.activeOffenders')

  SECONDS=0
  until [[ "${EXPECTED_COUNT:-0}" -gt 0 ]]; do
    echo 'waiting for count to be indexed ... '
    if (("${SECONDS}" >= "${TIMEOUT}")); then
      echo "Timed out getting index count."
      sendFailure
      exit 1
    fi
    sleep 60
    EXPECTED_COUNT=$(curl_json --no-fail "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1" | jq '._source.activeOffenders')
  done
  echo "Expected Count After Indexing ${EXPECTED_COUNT}"
}

wait_for_index_to_complete() {
  printf "\\nwaiting for indexing to complete ...\\n"
  parseAppInsightsConnectionString
  check_count_document

  ACTUAL_COUNT=$(curl_json --no-fail "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')

  until [[ "${ACTUAL_COUNT:-0}" -ge "${EXPECTED_COUNT}" ]]; do
    echo 'waiting for actual count to be at least expected count ...'
    if (("${SECONDS}" >= "${TIMEOUT}")); then
      echo "Indexing process timed out: Expected ${EXPECTED_COUNT} but got ${ACTUAL_COUNT}"
      sendFailure
      exit 1
    fi
    sleep 10
    ACTUAL_COUNT=$(curl_json --no-fail "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')
  done
  echo "Actual Count is ${ACTUAL_COUNT} => Expected ${EXPECTED_COUNT}"
}

sendSuccess() {
  printf "\\nSuccessfully completed indexing and alias switch\\n"
  now=$(date +'%FT%T%z')
  curl_json -XPOST "${APP_INSIGHTS_URL}" --data '{
    "name": "ProbationSearchIndexCompleted",
    "time": "'"${now}"'",
    "iKey": "'"${APP_INSIGHTS_KEY}"'",
    "tags": {
      "ai.cloud.role": "person-search-index-from-delius"
    },
    "data": {
       "baseType": "EventData",
       "baseData": {
          "ver": 1,
          "name": "ProbationSearchIndexCompleted",
          "properties": {
             "duration": "'"${SECONDS}"'",
             "count": "'"${ACTUAL_COUNT}"'"
          }
       }
    }
  }'
}

sendFailure() {
  printf "\\nFailed to complete indexing due to timeout\\n"
  now=$(date +'%FT%T%z')
  curl_json -XPOST "${APP_INSIGHTS_URL}" --data '{
    "name": "ProbationSearchIndexFailure",
    "time": "'"${now}"'",
    "iKey": "'"${APP_INSIGHTS_KEY}"'",
    "tags": {
      "ai.cloud.role": "person-search-index-from-delius"
    },
    "data": {
       "baseType": "ExceptionData",
       "baseData": {
          "ver": 1,
          "handledAt": "ProbationSearchIndexFailure",
          "properties": {
             "timeout": "'"${SECONDS}"'"
          },
          "exceptions": [
            {
               "typeName": "ProbationSearchIndexFailure",
               "message": "Indexing Process Timed Out",
               "hasFullStack": false,
               "parsedStack": [
               ]
            }
         ]
       }
    }
  }'
}

switch_aliases() {
  echo 'switching aliases'
  echo "primary => $STANDBY_INDEX"
  echo "standby => $PRIMARY_INDEX"

  curl_json -XPOST "${SEARCH_URL}"/_aliases --data '{
    "actions": [
      {
        "remove": {
          "index": "'"${PRIMARY_INDEX}"'",
          "alias": "'"${INDEX_PREFIX}-primary"'"
        }
      },
      {
        "remove": {
          "index": "'"${STANDBY_INDEX}"'",
          "alias": "'"${INDEX_PREFIX}-standby"'"
        }
      },
      {
        "add": {
          "index": "'"${STANDBY_INDEX}"'",
          "alias": "'"${INDEX_PREFIX}-primary"'"
        }
      },
      {
        "add": {
          "index": "'"${PRIMARY_INDEX}"'",
          "alias": "'"${INDEX_PREFIX}-standby"'"
        }
      }
    ]
  }'
  sendSuccess
}

get_current_indices && delete_ready_for_reindex && wait_for_index_to_complete && switch_aliases

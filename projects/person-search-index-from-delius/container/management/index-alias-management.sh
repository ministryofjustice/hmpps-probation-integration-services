#!/bin/bash
set -eo pipefail

function help {
  echo -e "\\nSCRIPT USAGE\\n"
  echo "-a The name of the PRIMARY alias"
  echo "-b The name of the STANDBY alias"
  echo "-u Search Host URL"
  exit 1
}

export SEARCH_URL="${SEARCH_INDEX_HOST}"
export PERSON_SEARCH_PRIMARY="person-search-primary"
export PERSON_SEARCH_STANDBY="person-search-standby"
export MAX_TIMEOUT=7200

while getopts a:b:h:u: FLAG; do
  case $FLAG in
  a)
    export PERSON_SEARCH_PRIMARY="${OPTARG}"
    ;;
  b)
    export PERSON_SEARCH_STANDBY="${OPTARG}"
    ;;
  h)
    help
    ;;
  u)
    export SEARCH_URL="$OPTARG"
    ;;
  \?) #unrecognized option - show help
    echo -e \\n"Option not allowed."
    help
    ;;
  esac
done

get_current_indices() {
  echo "Search URL: ${SEARCH_URL}"
  export PRIMARY_INDEX=$(curl -sSf -XGET -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/_alias/${PERSON_SEARCH_PRIMARY}" | jq keys | jq -r '.[0]')
  echo "Primary Index => ${PRIMARY_INDEX}"
  export STANDBY_INDEX=$(curl -sSf -XGET -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/_alias/${PERSON_SEARCH_STANDBY}" | jq keys | jq -r '.[0]')
  echo "Standby Index => ${STANDBY_INDEX}"

  if [ -z "$PRIMARY_INDEX" ] || [ -z "$STANDBY_INDEX" ] || [ "$PRIMARY_INDEX" = 'error' ] || [ "$STANDBY_INDEX" = 'error' ]; then
    echo "Unable to get Index Aliases."
    exit 1
  fi
}

delete_ready_for_reindex() {
  echo "deleting ${STANDBY_INDEX} ready for indexing ..."
  curl -sSf -XDELETE -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/${STANDBY_INDEX}"
  curl -sSf -XPUT -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/${STANDBY_INDEX}" -d '{
                                         "aliases": {
                                             "'"${PERSON_SEARCH_STANDBY}"'": {}
                                         }
                                      }'
}

parseAppInsightsConnectionString() {
    terms=$(echo "\"${APPLICATIONINSIGHTS_CONNECTION_STRING}\"" | tr ";" "\n")

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

    printf 'APP INSIGHTS URL:  %s' "$APP_INSIGHTS_URL"
}

check_count_document() {
  export EXPECTED_COUNT=$(curl -sS -XGET -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1" | jq '._source.activeOffenders')

  SECONDS=0
  until [[ "${EXPECTED_COUNT:-0}" -gt 0 ]]; do
    echo 'waiting for count to be indexed ... '
    if (("${SECONDS}" >= "${MAX_TIMEOUT}")); then
      echo "Timed out getting index count."
      sendFailure
      exit 1
    fi
    sleep 10
    export EXPECTED_COUNT=$(curl -sS -XGET -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1" | jq '._source.activeOffenders')
  done
  echo "Expected Count After Indexing ${EXPECTED_COUNT}"
}

wait_for_index_to_complete() {
  printf "\\nwaiting for indexing to complete ...\\n"
  parseAppInsightsConnectionString
  check_count_document

  ACTUAL_COUNT=$(curl -sS -XGET -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')

  until [[ "${ACTUAL_COUNT:-0}" -ge "${EXPECTED_COUNT}" ]]; do
    echo 'waiting for actual count to be at least expected count ...'
    if (("${SECONDS}" >= "${MAX_TIMEOUT}")); then
      echo "Indexing process timed out: Expected ${EXPECTED_COUNT} but got ${ACTUAL_COUNT}"
      sendFailure
      exit 1
    fi
    sleep 5
    ACTUAL_COUNT=$(curl -sS -XGET -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')
  done
  echo "Actual Count is ${ACTUAL_COUNT} => Expected ${EXPECTED_COUNT}"
}

sendSuccess() {
  printf "\\nSuccessfully completed indexing and alias switch\\n"
  now=$(date +'%FT%T%z')
  curl -sSf -XPOST -H "Content-Type: application/json" "${APP_INSIGHTS_URL}" -d '{
                                    "name": "ProbationSearchIndexCompleted",
                                    "time": "'"${now}"'",
                                    "iKey": "'"${APP_INSIGHTS_KEY}"'",
                                    "tags": {
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
  curl -sSf -XPOST -H "Content-Type: application/json" "${APP_INSIGHTS_URL}" -d '{
                                    "name": "ProbationSearchIndexFailure",
                                    "time": "'"${now}"'",
                                    "iKey": "'"${APP_INSIGHTS_KEY}"'",
                                    "tags": {
                                    },
                                    "data": {
                                       "baseType": "ExceptionData",
                                       "baseData": {
                                          "ver": 1,
                                          "handledAt": "ProbationSearchIndexFailure",
                                          "properties": {
                                             "timeout": "'"${MAX_TIMEOUT}"'"
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

  curl -sSf -XPOST -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}"/_aliases -d '{
                                      "actions": [
                                        {
                                          "remove": {
                                            "index": "*",
                                            "alias": "'"${PERSON_SEARCH_PRIMARY}"'"
                                          }
                                        },
                                        {
                                          "remove": {
                                            "index": "*",
                                            "alias": "'"${PERSON_SEARCH_STANDBY}"'"
                                          }
                                        },
                                        {
                                          "add": {
                                            "index": "'"${STANDBY_INDEX}"'",
                                            "alias": "'"${PERSON_SEARCH_PRIMARY}"'"
                                          }
                                        },
                                        {
                                          "add": {
                                            "index": "'"${PRIMARY_INDEX}"'",
                                            "alias": "'"${PERSON_SEARCH_STANDBY}"'"
                                          }
                                        }
                                      ]
                                    }'
  sendSuccess
}

get_current_indices && delete_ready_for_reindex && wait_for_index_to_complete && switch_aliases

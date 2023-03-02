#!/bin/bash
set -exo pipefail

function help {
  echo -e "\\nSCRIPT USAGE\\n"
  echo "-i The prefix of the primary and standby index name (e.g. 'person-search')"
  echo "-t The time (in seconds) to wait before attempting to switch aliases"
  echo "-u Search Host URL"
  exit 1
}

SEARCH_URL="${SEARCH_INDEX_HOST}"
TIMEOUT=7200

while getopts h:i:t:u: FLAG; do
  case $FLAG in
  h) help ;;
  i) INDEX_PREFIX="$OPTARG" ;;
  t) TIMEOUT="$OPTARG" ;;
  u) SEARCH_URL="$OPTARG" ;;
  \?) #unrecognized option - show help
    echo -e \\n"Option not allowed."
    help
    ;;
  esac
done
if [ -z "$INDEX_PREFIX" ]; then help; fail 'Missing -i'; fi

. functions.sh

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

function delete_ready_for_reindex() {
  echo "Deleting ${STANDBY_INDEX} ready for indexing ..."
  curl_json -XDELETE "${SEARCH_URL}/${STANDBY_INDEX}"
  curl_json -XPUT "${SEARCH_URL}/${STANDBY_INDEX}" --data '{"aliases": {"'"${INDEX_PREFIX}-primary"'": {}}}'
}

function wait_for_metadata_document() {
  echo 'Waiting for metadata document ...'
  SECONDS=0
  until curl_json "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1"; do
    if [ "$SECONDS" -gt "$TIMEOUT" ]; then fail 'Timed out getting metadata document' 'ProbationSearchIndexFailure'; fi
    sleep 60
  done
  LAST_ID=$(curl_json "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1" | jq '._source.lastId')
  echo "Metadata retrieved. The last id to be indexed will be $LAST_ID"
}

function wait_for_index_to_complete() {
  wait_for_metadata_document
  echo 'Waiting for indexing to complete ...'
  SECONDS=0
  until curl_json "${SEARCH_URL}/${STANDBY_INDEX}/_doc/${LAST_ID}"; do
    if [ "$SECONDS" -gt "$TIMEOUT" ]; then fail "Indexing process timed out. ID=${LAST_ID} was never indexed" 'ProbationSearchIndexFailure'; fi
    sleep 10
  done
  COUNT=$(curl_json "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')
  echo "Indexing complete. The $STANDBY_INDEX index now has $COUNT documents"
}

function switch_aliases() {
  echo 'Switching aliases ...'
  curl_json "${SEARCH_URL}/_aliases" --data '{
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
  echo "$STANDBY_INDEX is now the primary index, and $PRIMARY_INDEX is the standby"
  track_custom_event 'ProbationSearchIndexCompleted' '{"indexName": "'"$STANDBY_INDEX"'", "duration": "'"$SECONDS"'", "count": "'"$COUNT"'"}'
}

get_current_indices && delete_ready_for_reindex && wait_for_index_to_complete && switch_aliases

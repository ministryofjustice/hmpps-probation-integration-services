#!/bin/bash
set -eo pipefail
eval "$(sentry-cli bash-hook --no-environ)"
. "$(dirname -- "$0")/functions.sh"

function help {
  echo -e "\\nSCRIPT USAGE\\n"
  echo "-i The prefix of the primary and standby index name (e.g. 'person-search')"
  echo "-t The maximum time (in seconds) to wait for indexing to complete"
  echo "-u Search Host URL"
  exit 1
}

SEARCH_URL="${SEARCH_INDEX_HOST}"

while getopts h:i:t:u: FLAG; do
  case $FLAG in
  h) help ;;
  i) INDEX_PREFIX="$OPTARG" ;;
  t) REINDEXING_TIMEOUT="$OPTARG" ;;
  u) SEARCH_URL="$OPTARG" ;;
  \?) #unrecognized option - show help
    echo -e \\n"Option not allowed."
    help
    ;;
  esac
done
if [ -z "$INDEX_PREFIX" ]; then help; fail 'Missing -i'; fi
if [ -z "$REINDEXING_TIMEOUT" ]; then help; fail 'Missing -t'; fi

function stop_logstash() {
    exit_code=$?
    if [ "$exit_code" = '0' ]; then
      echo 'Gracefully stopping Logstash process...'
      pgrep java | xargs -n1 kill -TERM
    else
      echo 'Killing Logstash process...'
      _sentry_err_trap "${BASH_COMMAND:-unknown}" "$exit_code"
      pgrep java | xargs -n1 kill -KILL
    fi
    exit $exit_code
}
trap stop_logstash EXIT

function get_current_indices() {
  PRIMARY_INDEX=$(curl_json --retry 3 "${SEARCH_URL}/_alias/${INDEX_PREFIX}-primary" | jq -r 'keys[0]')
  STANDBY_INDEX=$(curl_json --retry 3 "${SEARCH_URL}/_alias/${INDEX_PREFIX}-standby" | jq -r 'keys[0]')
  echo "Search URL: ${SEARCH_URL}"
  echo "Primary Index => ${PRIMARY_INDEX}"
  echo "Standby Index => ${STANDBY_INDEX}"

  if [ -z "$PRIMARY_INDEX" ] || [ -z "$STANDBY_INDEX" ] || [ "$PRIMARY_INDEX" = 'error' ] || [ "$STANDBY_INDEX" = 'error' ]; then
    fail "Unable to get index aliases."
  fi
}

function delete_ready_for_reindex() {
  echo "Deleting ${STANDBY_INDEX} ready for indexing ..."
  curl_json -XDELETE "${SEARCH_URL}/${STANDBY_INDEX}"
  curl_json -XPUT "${SEARCH_URL}/${STANDBY_INDEX}" --data '{"aliases": {"'"${INDEX_PREFIX}-standby"'": {}}}'
}

function wait_for_index_to_complete() {
  echo 'Waiting for indexing to complete ...'
  SECONDS=0
  until [ "$(curl_json --no-show-error "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1/_source" | jq '.indexReady')" = 'true' ]; do
    if [ "$SECONDS" -gt "$REINDEXING_TIMEOUT" ]; then fail "Indexing process timed out." 'ProbationSearchIndexFailure'; fi
    sleep 60
  done
  COUNT=$(curl_json "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')
  echo "Indexing complete. The $STANDBY_INDEX index now has $COUNT documents"
}

function switch_aliases() {
  echo 'Switching aliases ...'
  curl_json "${SEARCH_URL}/_aliases" --data '{
    "actions": [
      { "remove": { "index": "'"${PRIMARY_INDEX}"'", "alias": "'"${INDEX_PREFIX}-primary"'" }},
      { "remove": { "index": "'"${STANDBY_INDEX}"'", "alias": "'"${INDEX_PREFIX}-standby"'" }},
      { "add": { "index": "'"${STANDBY_INDEX}"'", "alias": "'"${INDEX_PREFIX}-primary"'" }},
      { "add": { "index": "'"${PRIMARY_INDEX}"'", "alias": "'"${INDEX_PREFIX}-standby"'" }}
    ]
  }'
  echo "$STANDBY_INDEX is now the primary index, and $PRIMARY_INDEX is the standby"
  track_custom_event 'ProbationSearchIndexCompleted' '{"indexName": "'"$STANDBY_INDEX"'", "duration": "'"$SECONDS"'", "count": "'"$COUNT"'"}'
}

get_current_indices && delete_ready_for_reindex && wait_for_index_to_complete && switch_aliases

exit 0

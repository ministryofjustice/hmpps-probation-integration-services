#!/bin/bash
set -eo pipefail
eval "$(sentry-cli bash-hook --no-environ)"
source "$(dirname -- "$0")/functions.sh"

function help {
  echo -e "\\nSCRIPT USAGE\\n"
  echo "-i The prefix of the primary and standby index name (e.g. 'person-search')"
  echo "-t The maximum time (in seconds) to wait for indexing to complete"
  echo "-u Search Host URL"
  echo "$1"
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
if [ -z "$INDEX_PREFIX" ]; then help 'Missing -i'; fi
if [ -z "$REINDEXING_TIMEOUT" ]; then help 'Missing -t'; fi

function stop_logstash() {
  exit_code=$?
  echo 'Printing final stats...'
  curl --silent localhost:9600/_node/stats
  if [ "$exit_code" = '0' ]; then
    echo 'Gracefully stopping Logstash process...'
    pgrep java | xargs -n1 kill -TERM
  else
    echo 'Killing Logstash process...'
    _sentry_err_trap "${BASH_COMMAND:-unknown}" "$exit_code"
    pgrep java | xargs -n1 kill -KILL
  fi
  exit "$exit_code"
}
trap stop_logstash EXIT

function get_current_indices() {
  PRIMARY_INDEX=$(curl_json "${SEARCH_URL}/_alias/${INDEX_PREFIX}-primary" | jq -r 'keys[0]')
  STANDBY_INDEX=$(curl_json "${SEARCH_URL}/_alias/${INDEX_PREFIX}-standby" | jq -r 'keys[0]')
  echo "Search URL: ${SEARCH_URL}"
  echo "Primary Index => ${PRIMARY_INDEX}"
  echo "Standby Index => ${STANDBY_INDEX}"

  if [ -z "$PRIMARY_INDEX" ] || [ -z "$STANDBY_INDEX" ] || [ "$PRIMARY_INDEX" = 'error' ] || [ "$STANDBY_INDEX" = 'error' ]; then
    fail "Unable to get index aliases."
  fi
}

function get_status() {
  if [ -z "$ROUTING_REQUIRED" ]; then
    ROUTING_REQUIRED=$(curl_json "${SEARCH_URL}/${STANDBY_INDEX}/_mappings" | jq '.["'"${STANDBY_INDEX}"'"].mappings._routing.required // false')
    ROUTING_SUFFIX=$(if [ "$ROUTING_REQUIRED" = 'true' ]; then echo '?routing=-1'; else echo ''; fi)
  fi
  STATUS=$(curl_json --no-fail --no-show-error "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1${ROUTING_SUFFIX}")
}

function partially_completed() {
  if [ -z "$STATUS" ]; then get_status; fi
  if [ "$(jq '._source.indexReady' <<< "$STATUS")" = 'false' ]; then
    echo "Found result of a partially completed re-indexing job"
    return 0
  else
    echo "No partially completed re-indexing job found, starting a new job"
    return 1
  fi
}

function prepare_to_begin_indexing() {
  echo "Deleting and re-creating ${STANDBY_INDEX} ..."
  curl_json -XDELETE "${SEARCH_URL}/${STANDBY_INDEX}"
  curl_json -XPUT "${SEARCH_URL}/${STANDBY_INDEX}" --data '{"aliases": {"'"${INDEX_PREFIX}-standby"'": {}}}'
  echo "Disabling replicas and increasing refresh interval ..."
  curl_json -XPUT "${SEARCH_URL}/${STANDBY_INDEX}/_settings" --data '{"index": {"number_of_replicas": 0, "refresh_interval": "30m"}}'
}

function prepare_to_resume_indexing() {
  if [ -z "$STATUS" ]; then get_status; fi
  next_value=$(jq '._source.nextValue' <<< "$STATUS")
  resume_from_id=$((next_value-1))
  echo "Resuming from id=$resume_from_id ..."
  mkdir -p /usr/share/logstash/data/plugins/inputs/jdbc
  echo '--- !ruby/object:BigDecimal '"'0:$(printf "0.%se%d" "$resume_from_id" "${#resume_from_id}")'" > /usr/share/logstash/data/plugins/inputs/jdbc/logstash_jdbc_last_run
}

function wait_for_indexing_to_complete() {
  echo 'Waiting for indexing to complete ...'
  SECONDS=0
  get_status
  until [ "$(jq '._source.indexReady' <<< "$STATUS")" = 'true' ]; do
    if [ "$SECONDS" -gt "$REINDEXING_TIMEOUT" ]; then fail "Indexing process timed out after ${SECONDS}s." 'ProbationSearchIndexFailure'; fi
    sleep 60
    get_status
  done
  COUNT=$(curl_json "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')
  echo "Indexing is complete. The $STANDBY_INDEX index now has $COUNT documents"

  echo 'Merging segments to improve search performance ...'
  curl_json --no-fail -XPOST "${SEARCH_URL}/${STANDBY_INDEX}/_forcemerge?max_num_segments=10"
  echo 'Waiting for merge tasks to complete, this might take a while ...'
  sleep 60; until [ "$(curl --silent --show-error "${SEARCH_URL}/_cat/tasks?actions=*forcemerge*" | wc -l)" = '0' ]; do sleep 60; done
  echo 'Resetting replica count and refresh interval ...'
  curl_json -XPUT "${SEARCH_URL}/${STANDBY_INDEX}/_settings" --data '{"index": {"number_of_replicas": 1, "refresh_interval": "1s"}}'
  echo 'Waiting for replication to complete and cluster status to turn green ...'
  sleep 60; until [ "$(curl_json "${SEARCH_URL}/_cluster/health" | jq -r '.status')" = 'green' ]; do sleep 60; done

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
  exit 0
}


get_current_indices

if partially_completed
  then prepare_to_resume_indexing
  else prepare_to_begin_indexing
fi

wait_for_indexing_to_complete

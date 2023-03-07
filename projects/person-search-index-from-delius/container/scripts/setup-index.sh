#!/bin/bash
set -eo pipefail
eval "$(sentry-cli bash-hook)"
. "$(dirname -- "$0")/functions.sh"

function help {
  echo -e "\\nSCRIPT USAGE\\n"
  echo "-i The prefix of the primary and standby index name (e.g. 'person-search')"
  echo "-p The name of the pipeline"
  echo "-t The name of the template"
  echo "-u Search Host URL"
  exit 1
}

export SEARCH_URL="${SEARCH_INDEX_HOST}"

while getopts h:i:p:t:u: FLAG; do
  case $FLAG in
  h) help ;;
  i) export INDEX_PREFIX="${OPTARG}" ;;
  p) export PIPELINE_FILENAME="${OPTARG}" ;;
  t) export TEMPLATE_FILENAME="${OPTARG}" ;;
  u) export SEARCH_URL="$OPTARG" ;;
  \?) #unrecognized option - show help
    echo -e \\n"Option not allowed."
    help
    ;;
  esac
done
if [ -z "$INDEX_PREFIX" ]; then fail 'Missing -i'; fi
if [ -z "$SEARCH_URL" ]; then fail 'Missing -u'; fi

function create_pipeline() {
  if [ -n "${PIPELINE_FILENAME}" ]; then
    if [ ! -f "${PIPELINE_FILENAME}" ]; then fail "${PIPELINE_FILENAME} does not exist."; fi
    echo "Creating ${INDEX_PREFIX} pipeline ..."
    curl_json -XPUT "${SEARCH_URL}/_ingest/pipeline/${INDEX_PREFIX}-pipeline" --data @"${PIPELINE_FILENAME}"
  fi
}

function create_template() {
  if [ -n "${TEMPLATE_FILENAME}" ]; then
    if [ ! -f "${TEMPLATE_FILENAME}" ]; then fail "${TEMPLATE_FILENAME} does not exist."; fi
    echo "Creating ${INDEX_PREFIX} template ..."
    curl_json -XPUT "${SEARCH_URL}/_index_template/${INDEX_PREFIX}-template" --data @"${TEMPLATE_FILENAME}"
  fi
}

function create_indices() {
  echo "Creating ${INDEX_PREFIX} indices, if they don't already exist ..."
  curl_json -XPUT "${SEARCH_URL}/${INDEX_PREFIX}-a" --data '{"aliases": {"'"${INDEX_PREFIX}-primary"'": {}}}' --no-fail
  curl_json -XPUT "${SEARCH_URL}/${INDEX_PREFIX}-b" --data '{"aliases": {"'"${INDEX_PREFIX}-standby"'": {}}}' --no-fail
}

create_pipeline && create_template && create_indices

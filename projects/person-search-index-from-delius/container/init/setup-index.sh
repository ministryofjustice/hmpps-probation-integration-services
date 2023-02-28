#!/bin/bash
set -eo pipefail

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
if [ -z "$INDEX_PREFIX" ]; then echo 'Missing -i' >&2; exit 1; fi
if [ -z "$SEARCH_URL" ]; then echo 'Missing -u' >&2; exit 1; fi

function put_json() {
  curl --fail --silent --show-error --request PUT --header 'Content-Type: application/json' "$@"
  echo
}

function create_pipeline() {
  if [ -n "${PIPELINE_FILENAME}" ]; then
    if [ ! -f "${PIPELINE_FILENAME}" ]; then echo "${PIPELINE_FILENAME} does not exist." >&2; exit 1; fi
    echo "creating ${INDEX_PREFIX} pipeline ..."
    put_json "${SEARCH_URL}/_ingest/pipeline/${INDEX_PREFIX}-pipeline" --data @"${PIPELINE_FILENAME}"
  fi
}

function create_template() {
  if [ -n "${TEMPLATE_FILENAME}" ]; then
    if [ ! -f "${TEMPLATE_FILENAME}" ]; then echo "${TEMPLATE_FILENAME} does not exist." >&2; exit 1; fi
    echo "creating ${INDEX_PREFIX} template ..."
    put_json "${SEARCH_URL}/_index_template/${INDEX_PREFIX}-template" --data @"${TEMPLATE_FILENAME}"
  fi
}

function create_indices() {
  echo "creating ${INDEX_PREFIX} indices if they don't already exist ..."
  put_json "${SEARCH_URL}/${INDEX_PREFIX}-a" --data '{"aliases": {"'"${INDEX_PREFIX}-primary"'": {}}}' --no-fail
  put_json "${SEARCH_URL}/${INDEX_PREFIX}-b" --data '{"aliases": {"'"${INDEX_PREFIX}-standby"'": {}}}' --no-fail
}

create_pipeline && create_template && create_indices

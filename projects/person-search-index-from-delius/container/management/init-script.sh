#!/bin/bash
set -euo pipefail

function help {
  echo -e "\\nSCRIPT USAGE\\n"
  echo "-a The name of the PRIMARY alias"
  echo "-b The name of the STANDBY alias"
  echo "-p The name of the pipeline"
  echo "-t The name of the template"
  echo "-u Search Host URL"
  exit 1
}

export SEARCH_URL="${SEARCH_INDEX_HOST}"
export PERSON_SEARCH_PRIMARY="person-search-primary"
export PERSON_SEARCH_STANDBY="person-search-standby"

while getopts a:b:h:p:t:u: FLAG; do
  case $FLAG in
  a)
    export PIPELINE_FILENAME="${OPTARG}"
    ;;
  b)
    export TEMPLATE_FILENAME="${OPTARG}"
    ;;
  h)
    help
    ;;
  p)
    export PIPELINE_FILENAME="${OPTARG}"
    ;;
  t)
    export TEMPLATE_FILENAME="${OPTARG}"
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

function create_pipeline() {
  printf "creating person search pipeline ...\\n"
  curl -sSf -XPUT -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" -d @"${PIPELINE_FILENAME}" "${SEARCH_URL}/_ingest/pipeline/person-search-pipeline"
  printf "\\n"
}

function create_template() {
  printf "creating person search template ...\\n"
  curl -sSf -XPUT -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" -d @"${TEMPLATE_FILENAME}" "${SEARCH_URL}/_index_template/person-search-template"
  printf "\\n"
}

function create_indices() {
  curl -sS -XPUT -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/person-search-a" -d '{
                                           "aliases": {
                                               "'"${PERSON_SEARCH_PRIMARY}"'": {}
                                           }
                                        }'
  curl -sS -XPUT -H "Content-Type: application/json" -u "${SEARCH_INDEX_USERNAME}:${SEARCH_INDEX_PASSWORD}" "${SEARCH_URL}/person-search-b" -d '{
                                             "aliases": {
                                                 "'"${PERSON_SEARCH_STANDBY}"'": {}
                                             }
                                          }'
}

create_pipeline && create_template && create_indices

#!/bin/bash
set -euo pipefail

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
export MAX_TIMEOUT=4200

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
  export PRIMARY_INDEX=$(curl -sSf -XGET -H "Content-Type: application/json" "${SEARCH_URL}/_alias/${PERSON_SEARCH_PRIMARY}" | jq keys | jq -r '.[0]')
  echo "Primary Index => ${PRIMARY_INDEX}"
  export STANDBY_INDEX=$(curl -sSf -XGET -H "Content-Type: application/json" "${SEARCH_URL}/_alias/${PERSON_SEARCH_STANDBY}" | jq keys | jq -r '.[0]')
  echo "Standby Index => ${STANDBY_INDEX}"

  if [ -z "$PRIMARY_INDEX" ] || [ -z "$STANDBY_INDEX" ] || [ "$PRIMARY_INDEX" = 'error' ] || [ "$STANDBY_INDEX" = 'error' ]; then
    echo "Unable to get Index Aliases."
    exit 1
  fi
}

delete_ready_for_reindex() {
  echo "deleting ${STANDBY_INDEX} ready for indexing ..."
  curl -sSf -XDELETE -H "Content-Type: application/json" "${SEARCH_URL}/${STANDBY_INDEX}"
  curl -sSf -XPUT -H "Content-Type: application/json" "${SEARCH_URL}/${STANDBY_INDEX}" -d '{
                                         "aliases": {
                                             "'"${PERSON_SEARCH_STANDBY}"'": {}
                                         }
                                      }'
}

check_count_document() {
  export EXPECTED_COUNT=$(curl -sS -XGET -H "Content-Type: application/json" "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1" | jq '._source.activeOffenders')

  SECONDS=0
  until [[ "${EXPECTED_COUNT}" -gt 0 ]]; do
    echo 'waiting for count to be indexed ... '
    if (("${SECONDS}" >= "${MAX_TIMEOUT}")); then
      echo "Timed out getting index count."
      exit 1
    fi
    sleep 10
    export EXPECTED_COUNT=$(curl -sS -XGET -H "Content-Type: application/json" "${SEARCH_URL}/${STANDBY_INDEX}/_doc/-1" | jq '._source.activeOffenders')
  done
  echo "Expected Count After Indexing ${EXPECTED_COUNT}"
}

wait_for_index_to_complete() {
  printf "\\nwaiting for indexing to complete ...\\n"
  check_count_document

  ACTUAL_COUNT=$(curl -sS -XGET -H "Content-Type: application/json" "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')

  until [[ "${ACTUAL_COUNT}" -ge "${EXPECTED_COUNT}" ]]; do
    echo 'waiting for actual count to be at least expected count ...'
    if (("${SECONDS}" >= "${MAX_TIMEOUT}")); then
      echo "Indexing process timed out: Expected ${EXPECTED_COUNT} but got ${ACTUAL_COUNT}"
      exit 1
    fi
    sleep 5
    ACTUAL_COUNT=$(curl -sS -XGET -H "Content-Type: application/json" "${SEARCH_URL}/${STANDBY_INDEX}/_count" | jq '.count')
  done
  echo "Actual Count is ${ACTUAL_COUNT} => Expected ${EXPECTED_COUNT}"
}

switch_aliases() {
  echo 'switching aliases'
  echo "primary => $PRIMARY_INDEX"
  echo "standby => $STANDBY_INDEX"

  curl -sSf -XPOST -H "Content-Type: application/json" "${SEARCH_URL}"/_aliases -d '{
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

}

get_current_indices && delete_ready_for_reindex && wait_for_index_to_complete && switch_aliases

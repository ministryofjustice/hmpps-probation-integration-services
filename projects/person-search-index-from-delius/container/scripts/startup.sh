#!/bin/bash
set -eo pipefail

/scripts/setup-index.sh -i "$PERSON_INDEX_PREFIX" -p /pipelines/person/index/person-search-pipeline.json -t /pipelines/person/index/person-search-template.json
if [ "$PERSON_FULL_LOAD_PIPELINE_ENABLED" == 'enabled' ]; then
  /scripts/monitor-reindexing.sh -i "$PERSON_INDEX_PREFIX" -t "$PERSON_REINDEXING_TIMEOUT" &
fi

/scripts/setup-index.sh -i "$CONTACT_INDEX_PREFIX" -t /pipelines/contact/index/contact-search-template.json
if [ "$CONTACT_FULL_LOAD_PIPELINE_ENABLED" == 'enabled' ]; then
  /scripts/monitor-reindexing.sh -i "$CONTACT_INDEX_PREFIX" -t "$CONTACT_REINDEXING_TIMEOUT" &
fi

/usr/local/bin/docker-entrypoint

#!/bin/bash
set -eo pipefail
eval "$(sentry-cli bash-hook)"

pipelines=$(grep 'pipeline.id' /usr/share/logstash/config/pipelines.yml | sed 's/.*: //')
for pipeline in $pipelines; do
  if grep -v -q "$pipeline" <<<"$PIPELINES_ENABLED"; then
    # pipeline not enabled, remove from pipelines.yml
    sed -i "/$pipeline/,+1d" /usr/share/logstash/config/pipelines.yml
  fi
done

if grep --q 'person' <<<"$PIPELINES_ENABLED"; then
  /scripts/setup-index.sh -i "$PERSON_INDEX_PREFIX" -p /pipelines/person/index/person-search-pipeline.json -t /pipelines/person/index/person-search-template.json
  if grep --q 'person-full-load' <<<"$PIPELINES_ENABLED"; then
    /scripts/monitor-reindexing.sh -i "$PERSON_INDEX_PREFIX" -t "$PERSON_REINDEXING_TIMEOUT" &
  fi
fi

if grep --q 'contact' <<<"$PIPELINES_ENABLED"; then
  /scripts/setup-index.sh -i "$CONTACT_INDEX_PREFIX" -t /pipelines/contact/index/contact-search-template.json
  if grep --q 'contact-full-load' <<<"$PIPELINES_ENABLED"; then
    /scripts/monitor-reindexing.sh -i "$CONTACT_INDEX_PREFIX" -t "$CONTACT_REINDEXING_TIMEOUT" &
  fi
fi

/usr/local/bin/docker-entrypoint

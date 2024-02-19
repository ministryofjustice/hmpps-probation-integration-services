#!/bin/bash
set -eo pipefail
eval "$(sentry-cli bash-hook --no-environ)"
. "$(dirname -- "$0")/functions.sh"

# Workaround for intermittent DNS resolution failure immediately after container start
if [ -z "$SEARCH_INDEX_HOST" ]; then fail 'Missing environment variable: SEARCH_INDEX_HOST'; fi
curl_json --retry 3 "$SEARCH_INDEX_HOST"

# Configure cluster settings
curl_json -XPUT "${SEARCH_INDEX_HOST}/_cluster/settings" --data '{
  "persistent": {
    "action.auto_create_index": "false"
  }
}'

pipelines=$(grep 'pipeline.id' /usr/share/logstash/config/pipelines.yml | sed 's/.*: //')
for pipeline in $pipelines; do
  if grep -v -q "$pipeline" <<<"$PIPELINES_ENABLED"; then
    # pipeline not enabled, remove from pipelines.yml
    sed -i "/$pipeline/,+1d" /usr/share/logstash/config/pipelines.yml
  fi
done

if grep -q 'person' <<<"$PIPELINES_ENABLED"; then
  /scripts/setup-index.sh -i "$PERSON_INDEX_PREFIX" -p /pipelines/person/index/person-search-pipeline.json -t /pipelines/person/index/person-search-template.json
  if grep -q 'person-full-load' <<<"$PIPELINES_ENABLED"; then
    sentry-cli monitors run "$PERSON_REINDEXING_SENTRY_MONITOR_ID" -- /scripts/monitor-reindexing.sh -i "$PERSON_INDEX_PREFIX" -t "$PERSON_REINDEXING_TIMEOUT" &
  fi
fi

if grep -q 'contact' <<<"$PIPELINES_ENABLED"; then
  /scripts/setup-index.sh -i "$CONTACT_INDEX_PREFIX" -t /pipelines/contact/index/contact-search-template.json
  if grep -q 'contact-full-load' <<<"$PIPELINES_ENABLED"; then
    sentry-cli monitors run "$CONTACT_REINDEXING_SENTRY_MONITOR_ID" -- /scripts/monitor-reindexing.sh -i "$CONTACT_INDEX_PREFIX" -t "$CONTACT_REINDEXING_TIMEOUT" &
  fi
fi

/usr/local/bin/docker-entrypoint

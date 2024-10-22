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
    "action.auto_create_index": "false",
    "plugins.ml_commons.only_run_on_ml_node": "false",
    "plugins.ml_commons.model_access_control_enabled": "false",
    "plugins.ml_commons.native_memory_threshold": "90"
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
  /scripts/setup-index.sh -i "$CONTACT_INDEX_PREFIX" -t /pipelines/contact/index/index-template-keyword.json
  if grep -q 'contact-full-load' <<<"$PIPELINES_ENABLED"; then
    sentry-cli monitors run "$CONTACT_REINDEXING_SENTRY_MONITOR_ID" -- /scripts/monitor-reindexing.sh -i "$CONTACT_INDEX_PREFIX" -t "$CONTACT_REINDEXING_TIMEOUT" &
  fi

  # Setup semantic search for contacts
  export BEDROCK_MODEL_NAME=amazon.titan-embed-text-v2:0
  /scripts/deploy-semantic-model.sh
  model_id=$(curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/models/_search" --data "{\"query\":{\"match\":{\"name.keyword\":\"bedrock-${BEDROCK_MODEL_NAME}\"}}}" | jq -r '.hits.hits[0]._id // ""')
  export model_id
  echo "Deployed semantic search model. model_id=${model_id}"
  envsubst < /pipelines/contact/index/ingest-pipeline.tpl.json > /pipelines/contact/index/ingest-pipeline.json
  envsubst < /pipelines/contact/index/search-pipeline.tpl.json > /pipelines/contact/index/search-pipeline.json
  /scripts/setup-index.sh -i "contact-semantic-search" -p /pipelines/contact/index/ingest-pipeline.json -s /pipelines/contact/index/search-pipeline.json -t /pipelines/contact/index/index-template-semantic.json
fi

echo Starting Logstash...
/usr/local/bin/docker-entrypoint

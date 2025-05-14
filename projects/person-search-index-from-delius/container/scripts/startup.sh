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
    "plugins.ml_commons.native_memory_threshold": "90",
    "plugins.ml_commons.trusted_connector_endpoints_regex": [
        "^https://bedrock-runtime\\..*[a-z0-9-]\\.amazonaws\\.com/.*$",
        "^https://runtime\\.sagemaker\\..*[a-z0-9-]\\.amazonaws\\.com/.*$"
    ]
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

if grep -q 'contact-keyword' <<<"$PIPELINES_ENABLED"; then
  /scripts/setup-index.sh -i "$CONTACT_KEYWORD_INDEX_PREFIX" -t /pipelines/contact-keyword/index/index-template-keyword.json
  if grep -q 'contact-keyword-full-load' <<<"$PIPELINES_ENABLED"; then
    sentry-cli monitors run "$CONTACT_REINDEXING_SENTRY_MONITOR_ID" -- /scripts/monitor-reindexing.sh -i "$CONTACT_KEYWORD_INDEX_PREFIX" -t "$CONTACT_KEYWORD_REINDEXING_TIMEOUT" &
  fi
fi

if grep -q 'contact-semantic' <<<"$PIPELINES_ENABLED"; then
  # Setup semantic search for contacts
  /scripts/deploy-semantic-model.sh
  model_id=$(curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/models/_search" --data "{\"query\":{\"match\":{\"name.keyword\":\"sagemaker-embeddings\"}}}" | jq -r '.hits.hits[0]._id // ""')
  export model_id
  echo "Deployed semantic search model. model_id=${model_id}"
  envsubst < /pipelines/contact-semantic/index/ingest-pipeline.tpl.json > /pipelines/contact-semantic/index/ingest-pipeline.json
  envsubst < /pipelines/contact-semantic/index/search-pipeline.tpl.json > /pipelines/contact-semantic/index/search-pipeline.json

  /scripts/setup-index.sh -i "$CONTACT_SEMANTIC_INDEX_PREFIX" \
    -p /pipelines/contact-semantic/index/ingest-pipeline.json \
    -s /pipelines/contact-semantic/index/search-pipeline.json \
    -t /pipelines/contact-semantic/index/index-template-semantic.json \
    -y /pipelines/contact-semantic/index/index-state-management-policy.json
  if grep -q 'contact-semantic-full-load' <<<"$PIPELINES_ENABLED"; then
    sentry-cli monitors run "$CONTACT_REINDEXING_SENTRY_MONITOR_ID" -- /scripts/monitor-reindexing.sh -i "$CONTACT_SEMANTIC_INDEX_PREFIX" -t "$CONTACT_SEMANTIC_REINDEXING_TIMEOUT" &
    # export the name of the standby index to be referenced in logstash-full-load.conf, because the max_token_count check in the text_chunking ingest processor does not account for aliases (as of OpenSearch 2.19)
    CONTACT_SEMANTIC_INDEX_STANDBY=$(curl_json "${SEARCH_INDEX_HOST}/_alias/${CONTACT_SEMANTIC_INDEX_PREFIX}-standby" | jq -r 'keys[0]')
    export CONTACT_SEMANTIC_INDEX_STANDBY
  fi
fi

echo Starting Logstash...
/usr/local/bin/docker-entrypoint

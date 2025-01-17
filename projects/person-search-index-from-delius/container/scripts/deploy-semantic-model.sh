#!/bin/bash
set -eo pipefail
eval "$(sentry-cli bash-hook --no-environ)"
. "$(dirname -- "$0")/functions.sh"

## Create model group if it doesn't exist
echo Searching for existing model group...
model_group_id=$(curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/model_groups/_search" --data '{"query":{"match":{"name":"sagemaker_model_group"}}}' | jq -r '.hits.hits[0]._id // ""')
if [ -z "$model_group_id" ]; then
  echo Creating model group...
  model_group=$(curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/model_groups/_register" --data '{
    "name": "sagemaker_model_group",
    "description": "A model group for SageMaker models"
  }')
  if [ "$(jq -r '.status' <<<"$model_group")" != "CREATED" ]; then fail "Failed to create model group: $model_group"; fi
  model_group_id=$(jq -r '.model_group_id' <<<"$model_group")
else
  echo "Found model group with id=$model_group_id"
fi

## Create SageMaker connector if it doesn't exist
echo Searching for existing connector...
connector_id=$(curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/connectors/_search" --data "{\"query\":{\"match\":{\"name.keyword\":\"sagemaker-embeddings\"}}}" | jq -r '.hits.hits[0]._id // ""')
if [ -z "$connector_id" ]; then
  echo Creating connector...
  connector_body=$(envsubst < /pipelines/contact/index/sagemaker-connector.json)
  connector_id=$(curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/connectors/_create" --data "$connector_body" | jq -r '.connector_id')
else
  echo "Found connector with id=$connector_id"
fi

## Register model if it doesn't exist
model_body="{
  \"name\": \"sagemaker-embeddings\",
  \"description\": \"SageMaker embedding model\",
  \"function_name\": \"remote\",
  \"model_group_id\": \"${model_group_id}\",
  \"connector_id\": \"${connector_id}\"
}"
model_id=$(curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/models/_search" --data "{\"query\":{\"match\":{\"name.keyword\":\"sagemaker-embeddings\"}}}" | jq -r '.hits.hits[0]._id // ""')
if [ -z "$model_id" ]; then
  echo Registering model...
  curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/models/_register" --data "${model_body}"
else
  echo Updating model...
  curl_json -XPUT "${SEARCH_INDEX_HOST}/_plugins/_ml/models/${model_id}" --data "${model_body}" || echo 'Warning: Failed to update model' >&2
fi

## Deploy model
echo "Deploying model with id=${model_id}..."
curl_json -XPOST "${SEARCH_INDEX_HOST}/_plugins/_ml/models/${model_id}/_deploy" || echo 'Warning: Failed to deploy model' >&2

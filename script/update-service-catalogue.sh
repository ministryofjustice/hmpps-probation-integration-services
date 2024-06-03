#!/usr/bin/env bash
set -euo pipefail
##
## Add a new project to the service catalogue.
##
## Example usage:
##   PROJECT_NAME=approved-premises-and-delius ./script/update-service-catalogue.sh
##

[ -z "$PROJECT_NAME" ] && echo "Missing PROJECT_NAME" && exit 1
[ -z "$SERVICE_CATALOGUE_API_KEY" ] && echo "Missing SERVICE_CATALOGUE_API_KEY" && exit 1

api_url="https://service-catalogue.hmpps.service.justice.gov.uk/v1"
product_code="HMPPS518"
product_id=$(curl -fsS -H "Authorization: Bearer $SERVICE_CATALOGUE_API_KEY" "$api_url/products?filters\[p_id\]=$product_code" | jq -r '.data[0].id // ""')
component=$(curl -fsS -H "Authorization: Bearer $SERVICE_CATALOGUE_API_KEY" "$api_url/components?filters\[name\]=$PROJECT_NAME")

# Get ids for existing environments
ENVIRONMENTS=${ENVIRONMENTS:-[]}
for env in dev preprod prod; do
  env_id=$(echo "$component" | jq -r '.data[0].environments | select(.name == $env) | .id' --arg env "$env")
  namespace_id=$(curl -fsS -H "Authorization: Bearer $SERVICE_CATALOGUE_API_KEY" "$api_url/namespaces?filters\[name\]=hmpps-probation-integration-services-$env" | jq '.data[0].id')
  if [ -n "$env_id" ]; then ENVIRONMENTS=$(echo "$ENVIRONMENTS" | jq -r 'map(select(.name == $env) += {"id": $env_id})' --arg env "$env" --arg env_id "$env_id"); fi
  if [ -n "$namespace_id" ]; then ENVIRONMENTS=$(echo "$ENVIRONMENTS" | jq -r 'map(select(.name == $env) += {"ns": $namespace_id})' --arg env "$env" --arg namespace_id "$namespace_id"); fi
done

data=$(jq -n '{"data": $ARGS.named}' \
    --arg product "$product_id" \
    --arg name "$PROJECT_NAME" \
    --arg title "$PROJECT_TITLE" \
    --arg github_repo hmpps-probation-integration-services \
    --arg part_of_monorepo true \
    --arg path_to_project "projects/$PROJECT_NAME" \
    --arg path_to_helm_dir "projects/$PROJECT_NAME/deploy" \
    --argjson environments "$ENVIRONMENTS" \
    --argjson jira_project_keys '["PI"]'
)

component_id=$(echo "$component" | jq -r '.data[0].id // ""')
if [ -z "$component_id" ]; then
    echo "Adding component $PROJECT_NAME to hmpps-service-catalogue"
    curl -XPOST -fsS -H "Authorization: Bearer $SERVICE_CATALOGUE_API_KEY" "$api_url/components" --json "$data"
else
    echo "Updating component $PROJECT_NAME ($component_id) in hmpps-service-catalogue"
    curl -XPUT -fsS -H "Authorization: Bearer $SERVICE_CATALOGUE_API_KEY" "$api_url/components/$component_id" --json "$data"
fi

echo "Done: https://developer-portal.hmpps.service.justice.gov.uk/components/$PROJECT_NAME"
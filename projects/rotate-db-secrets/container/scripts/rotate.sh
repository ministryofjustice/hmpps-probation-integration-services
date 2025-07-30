#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n'

[ -z "$NAMESPACE" ] && echo "Missing NAMESPACE" && exit 1

COMMON_STRING=$(kubectl --namespace=$NAMESPACE get secret common -o json \
| jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"' | grep DB_URL)

for PROJECT_NAME in $(kubectl --namespace=$NAMESPACE get secrets -o name | grep "\-database$" | sed "s/secret\///g" | sed "s/-database//g")
do
  PROJECT_NAME="find-and-refer-and-delius"
  echo "Updating DB secret for $PROJECT_NAME"

  NEW_SECRET=$(pwgen -N1 16)

  PROJECT_UN_STRING=$(kubectl --namespace=$NAMESPACE get secret "$PROJECT_NAME-database" -o json \
  | jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"'  | grep DB_USERNAME)

  PROJECT_PW_STRING=$(kubectl --namespace=$NAMESPACE get secret "$PROJECT_NAME-database" -o json \
  | jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"'  | grep DB_PASSWORD)

  DB_URL=$(echo $COMMON_STRING | sed 's/DB_URL=jdbc:oracle:thin://g')
  DB_UN=$(echo $PROJECT_UN_STRING | sed 's/DB_USERNAME=//g')
  DB_PW=$(echo $PROJECT_PW_STRING | sed 's/DB_PASSWORD=//g')
  CONN_STR="$DB_UN/$DB_PW$DB_URL"

  SQL="select count(*) from offender o; select count(*) from alias a;"

  echo "$SQL" | sqlplus -s "$CONN_STR"

  break

  #kubectl rollout restart "deployment/$PROJECT_NAME" -n hmpps-probation-integration-services-dev

done






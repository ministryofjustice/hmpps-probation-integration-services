#!/usr/bin/env bash
set -euo pipefail

[ -z "$NAMESPACE" ] && echo "Missing NAMESPACE" && exit 1

COMMON_STRING=$(kubectl --namespace=$NAMESPACE get secret common -o json \
| jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"' | grep DB_URL)

PROJECTS=$(kubectl --namespace=hmpps-probation-integration-services-dev get secrets -o name | grep "\-database$" | sed "s/secret\///g" | sed "s/-database//g")

for PROJECT_NAME in "${PROJECTS[@]}"
do
  echo "$PROJECT_NAME"
  PROJECT_UN_STRING=$(kubectl --namespace=$NAMESPACE get secret $PROJECT_NAME-database -o json \
  | jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"'  | grep DB_USERNAME)

  PROJECT_PW_STRING=$(kubectl --namespace=$NAMESPACE get secret $PROJECT_NAME-database -o json \
  | jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"'  | grep DB_PASSWORD)

  DB_URL=$(echo $COMMON_STRING | sed 's/DB_URL=jdbc:oracle:thin://g')
  DB_UN=$(echo $PROJECT_UN_STRING | sed 's/DB_USERNAME=//g')
  DB_PW=$(echo $PROJECT_PW_STRING | sed 's/DB_PASSWORD=//g')
  CONN_STR="$DB_UN/$DB_PW$DB_URL"
  POD_NAME="rotate-$PROJECT_NAME"

  SQL="select count(*) from offender o;"

  #echo "Rotation pod is ready"
  #echo "Executing command"
  echo "$SQL" | sqlplus -s "$CONN_STR"
  #echo "Command successful"
done






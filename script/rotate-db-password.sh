#!/usr/bin/env bash
set -euo pipefail
##
## Rotate Password for service
##
## Example usage:
##   PROJECT_NAME=appointment-reminders-and-delius NAMESPACE=hmpps-probation-integration-services-dev ./rotate-db-password.sh
##

[ -z "$PROJECT_NAME" ] && echo "Missing PROJECT_NAME" && exit 1
[ -z "$NAMESPACE" ] && echo "Missing NAMESPACE" && exit 1

COMMON_STRING=$(kubectl --namespace=$NAMESPACE get secret common -o json \
| jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"' | grep DB_URL)

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

kubectl run $POD_NAME --namespace=$NAMESPACE --overrides="{\"spec\":{\"securityContext\": {\"runAsUser\":1003 } } }" --image=ghcr.io/oracle/oraclelinux8-instantclient:21 -- sleep infinity
kubectl wait --namespace="$NAMESPACE" --for=condition=ready pod "$POD_NAME"
echo "Rotation pod is ready"
echo "Executing command"
kubectl exec -it --namespace="$NAMESPACE" $POD_NAME -- bash -c "echo '$SQL' | sqlplus -s '$CONN_STR'"
echo "Command successful"
echo "Deleting pod"

kubectl delete --namespace="$NAMESPACE" pod "$POD_NAME"

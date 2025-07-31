#!/usr/bin/env bash

# RUN WITH CARE. Ensure that the prob_int_service_prof profile is set on all service users
# This script performs the following for each probation integration service that has a database credential:
# 1. Gets the current username and password
# 2. Generates a new password
# 3. Makes a DB call to set the new password
# 4. Updates the kubernetes secret
# 5. Restarts the service

set -euo pipefail
IFS=$'\n'

[ -z "$NAMESPACE" ] && echo "Missing NAMESPACE" && exit 1

COMMON_STRING=$(kubectl --namespace=$NAMESPACE get secret common -o json \
| jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"' | grep DB_URL)

for PROJECT_NAME in $(kubectl --namespace=$NAMESPACE get secrets -o name | grep "\-database$" | sed "s/secret\///g" | sed "s/-database//g")
do
  # TESTING WITH JUST ONE service - to run all, uncomment this and remove the break at the end of the do loop
  PROJECT_NAME="find-and-refer-and-delius"

  PROJECT_UN_STRING=$(kubectl --namespace=$NAMESPACE get secret "$PROJECT_NAME-database" -o json \
  | jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"'  | grep DB_USERNAME)

  PROJECT_PW_STRING=$(kubectl --namespace=$NAMESPACE get secret "$PROJECT_NAME-database" -o json \
  | jq -r '.data | map_values(@base64d) | to_entries[] | "\(.key)=\(.value)"'  | grep DB_PASSWORD)

  DB_URL=$(echo $COMMON_STRING | sed 's/DB_URL=jdbc:oracle:thin://g')
  DB_UN=$(echo $PROJECT_UN_STRING | sed 's/DB_USERNAME=//g')
  DB_PW=$(echo $PROJECT_PW_STRING | sed 's/DB_PASSWORD=//g')

  # DB url fo running the script locally and against the database via the bastion
  DB_URL_LOCAL=$(echo $DB_URL | sed 's/delius-db-1.test.delius.probation.hmpps.dsd.io/localhost/g')

  # Setting the connection
  # For local use
  # CONN_STR="$DB_UN/$DB_PW$DB_LOCAL"
  # Switch to using local DB connection (via the bastion)

  CONN_STR="$DB_UN/$DB_PW$DB_URL_LOCAL"
  echo "$CONN_STR"

  NEW_DB_PW=$(pwgen -N1 16)
  EXISTING_SECRET=$DB_PW

  #  Testing with a basic SQL statement to prove connectivity
  #  When we are ready run this e2e with the actual password update - change SQL code to below tp perform the DB change
  #
  #  SQL="whenever sqlerror exit sql.sqlcode;
  #       ALTER USER manage-supervisions-and-delius-database IDENTIFIED BY $NEW_DB_PW;
  #  "

  SQL="whenever sqlerror exit sql.sqlcode;
       SELECT d.PROFILE FROM DBA_USERS d where d.USERNAME = '$DB_UN';
       select count(*) from alias a;
  "
  echo "$SQL" | sqlplus -s "$CONN_STR"

  echo "SQL Successful - updating secret for $PROJECT_NAME"

  # When we are ready to run this for real use $NEW_DB_PW instead of $EXISTING_SECRET - remove -testing to update actual secret
  kubectl -n $NAMESPACE create secret generic "$PROJECT_NAME-database-testing" \
    --from-literal "DB_USERNAME=${PROJECT_NAME//-/_}" \
    --from-literal "DB_PASSWORD=$EXISTING_SECRET" \
    --save-config \
    --dry-run=client -o yaml | kubectl apply -f -

  echo "Secret created - restarting $PROJECT_NAME"

  #  When we are ready run this e2e with the actual update, uncomment this line to restart the service
  #kubectl rollout restart "deployment/$PROJECT_NAME" -n hmpps-probation-integration-services-dev

  echo "SQL Successful - restart service"

  # TESTING WITH JUST ONE service - to run all, uncomment this and remove the break at the end of the do loop
  break

done






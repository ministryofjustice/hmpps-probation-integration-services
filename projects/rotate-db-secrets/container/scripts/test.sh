#!/usr/bin/env bash
set -euo pipefail

PROJECTS=$(kubectl --namespace=hmpps-probation-integration-services-dev get secrets -o name | grep "\-database$" | sed "s/secret\///g" | sed "s/-database//g")

for PROJECT in "${PROJECTS[@]}"
do
   echo "$PROJECT"
   # or do whatever with individual element of the array
done





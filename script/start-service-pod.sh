#!/usr/bin/env bash
set -eo pipefail
##
## Start a long-running Kubernetes pod in a given namespace.
##
## Example usage:
##   NAMESPACE=hmpps-probation-integration POD_NAME="$USER" ./script/start-service-pod.sh
##

if [ -z "$POD_NAME" ]; then echo "Missing POD_NAME"; exit 1; fi
if [ -z "$NAMESPACE" ]; then echo "Missing NAMESPACE"; exit 1; fi
if [ -n "$SERVICE_ACCOUNT_NAME" ]; then overrides="{\"spec\":{\"serviceAccount\": \"$SERVICE_ACCOUNT_NAME\"}}"; else overrides="{}"; fi

echo "Starting service pod '$POD_NAME'"
function delete_pod() { kubectl --namespace="$NAMESPACE" delete pod "$POD_NAME"; }
trap delete_pod SIGTERM SIGINT

kubectl run "$POD_NAME" --namespace="$NAMESPACE" --overrides="$overrides" --image=ghcr.io/ministryofjustice/hmpps-devops-tools:latest -- sleep infinity
kubectl wait --namespace="$NAMESPACE" --for=condition=ready pod "$POD_NAME"

echo "Service pod is ready"
#!/usr/bin/env bash
set -euo pipefail
##
## Start a long-running Kubernetes pod in a given namespace.
##
## Example usage:
##   NAMESPACE=hmpps-probation-integration POD_NAME="$USER" ./script/start-service-pod.sh
##

[ -z "$POD_NAME" ] && echo "Missing POD_NAME" && exit 1
[ -z "$NAMESPACE" ] && echo "Missing NAMESPACE" && exit 1

echo "Starting service pod '$POD_NAME'"
function delete_pod() { kubectl --namespace="$NAMESPACE" delete pod "$POD_NAME"; }
trap delete_pod SIGTERM SIGINT

kubectl run "$POD_NAME" --namespace="$NAMESPACE" --image=ghcr.io/ministryofjustice/hmpps-devops-tools:latest -- sleep infinity
kubectl wait --namespace="$NAMESPACE" --for=condition=ready pod "$POD_NAME"

echo "Service pod is ready"
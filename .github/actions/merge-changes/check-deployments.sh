#!/usr/bin/env bash

function fromJsonArray() {
  jq --raw-output '.[]'
}

function toJsonArray() {
  jq --raw-input . | jq --slurp --compact-output .
}

function deploymentEnabled() {
  env=$1
  while IFS= read -r project; do
    file="projects/$project/deploy/values-$env.yml"
    if [ -f "$file" ] && ! grep -q '^enabled: false' "$file"; then echo "$project"; fi
  done
}

echo "dev=$(echo -n "$PROJECTS" | fromJsonArray | deploymentEnabled dev | toJsonArray)" | tee -a "$GITHUB_OUTPUT"
echo "preprod=$(echo -n "$PROJECTS" | fromJsonArray | deploymentEnabled preprod | toJsonArray)" | tee -a "$GITHUB_OUTPUT"
echo "prod=$(echo -n "$PROJECTS" | fromJsonArray | deploymentEnabled prod | toJsonArray)" | tee -a "$GITHUB_OUTPUT"

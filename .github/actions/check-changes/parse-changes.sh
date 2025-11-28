#!/usr/bin/env bash

function toJsonArray() {
  jq --raw-input . | jq --slurp --compact-output .
}

function topLevelChanges() {
  sed -E 's|^projects/([^/$]*).*|\1|' | sort -u
}

if [ "$COMMONS_CHANGED" == "true" ]; then
  projects=$(find projects -mindepth 1 -maxdepth 1 -printf "%f\n" | toJsonArray)
  echo "Changes detected in common files, rebuild/deploy everything"
  echo "projects=$projects" | tee -a "$GITHUB_OUTPUT"
elif [ -n "$PROJECT_FILES" ]; then
  projects=$(echo "$PROJECT_FILES" | xargs -n1 | topLevelChanges | toJsonArray)
  echo "Changes detected in: $projects"
  echo "projects=$projects" | tee -a "$GITHUB_OUTPUT"
else
  echo "No changes detected"
  echo "projects=[]" | tee -a "$GITHUB_OUTPUT"
fi

#!/usr/bin/env bash

function toJsonArray() {
  jq --raw-input . | jq --slurp --compact-output .
}

function topLevelChanges() {
  sed -E 's|^projects/([^/$]*).*|\1|' | sort -u
}

if [ "$COMMONS_CHANGED" == "true" ]; then
  projects=$(cd projects && echo * | xargs -n1 | toJsonArray)
  echo "Changes detected in common files, rebuild/deploy everything"
  echo "projects=$projects" >> $GITHUB_OUTPUT
elif [ -n "$PROJECT_FILES" ]; then
  projects=$(echo $PROJECT_FILES | xargs -n1 | topLevelChanges | toJsonArray)
  echo "Changes detected in: $projects"
  echo "projects=$projects" >> $GITHUB_OUTPUT
fi

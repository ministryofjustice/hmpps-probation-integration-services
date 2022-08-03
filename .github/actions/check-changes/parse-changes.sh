#!/usr/bin/env bash

function toJsonArray() {
  jq --raw-input . | jq --slurp --compact-output .
}

function topLevelChanges() {
  sed -E 's|^projects/([^/$]*).*|\1|' | sort -u
}

function isActive() {
  grep -v 'workforce-allocations-to-delius'
}

if [ "$COMMONS_CHANGED" == "true" ]; then
  projects=$(cd projects && echo * | xargs -n1 | isActive | toJsonArray)
  echo "Changes detected in common files, rebuild/deploy everything"
  echo "::set-output name=projects::$projects"
elif [ -n "$PROJECT_FILES" ]; then
  projects=$(echo $PROJECT_FILES | xargs -n1 | topLevelChanges | isActive | toJsonArray)
  echo "Changes detected in: $projects"
  echo "::set-output name=projects::$projects"
fi

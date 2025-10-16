#!/usr/bin/env bash

all_projects="$(find projects -mindepth 1 -maxdepth 1 -printf "%f\n" | jq --raw-input . | jq --slurp --compact-output .)"

# Return all projects
if [ "$INPUT" == 'All' ]; then output="$all_projects"

# Check if input is already JSON,
elif jq <<< "$INPUT" &>/dev/null; then output="$INPUT"

# Wrap in JSON array
else output="$(jq --compact-output -n --arg INPUT "$INPUT" '[$INPUT]')"; fi

# Filter to existing projects and sort alphabetically
output="$(jq --compact-output -n --argjson A "$output" --argjson B "$all_projects" '$A - ($A - $B) | sort')"

# Output
echo "projects=$output" | tee -a "$GITHUB_OUTPUT"
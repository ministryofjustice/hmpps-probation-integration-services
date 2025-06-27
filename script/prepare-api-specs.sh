#!/usr/bin/env sh
set -eu

wait_for_api() {
  timeout 300 sh -c "until curl -sf \"$1\" >/dev/null; do sleep 5; done"
}
wait_for_api_to_fail() {
  timeout 300 sh -c "while curl -sf \"$1\" >/dev/null; do sleep 5; done"
}

for project_path in doc/tech-docs/source/projects/*; do
  if [ ! -d "$project_path" ]; then continue; fi
  project_name="$(basename "$project_path")"

  if [ -f "$project_path/api-reference.html.md.erb" ]; then has_rest_api=true; else has_rest_api=false; fi
  if [ -f "$project_path/asyncapi-reference.html.md.erb" ]; then has_async_api=true; else has_async_api=false; fi

  if [ "$has_rest_api" != "true" ] && [ "$has_async_api" != "true" ]; then
    echo "No OpenAPI or AsyncAPI config found for $project_name; skipping."
    continue
  fi

  echo "Starting Spring Boot app for $project_name ..."
  SPRING_PROFILES_ACTIVE=dev "./gradlew" "${project_name}:bootRun" &
  GRADLE_PID=$!
  echo "Started with pid=$GRADLE_PID"

  mkdir -p "$project_path/assets"
  if [ "$has_rest_api" = "true" ]; then
    if ! wait_for_api "http://localhost:8080/v3/api-docs.yaml"; then
      echo "ERROR: Timed out waiting for OpenAPI endpoint for $project_name"
      kill "$GRADLE_PID" || true
      exit 1
    fi
    echo "Downloading OpenAPI specs ..."
    curl -f http://localhost:8080/v3/api-docs -o "$project_path/assets/api-docs.json"
    curl -f http://localhost:8080/v3/api-docs.yaml -o "$project_path/assets/api-docs.yaml"
  fi
  if [ "$has_async_api" = "true" ]; then
    if ! wait_for_api "http://localhost:8080/docs/asyncapi"; then
      echo "ERROR: Timed out waiting for AsyncAPI endpoint for $project_name"
      kill "$GRADLE_PID" || true
      exit 1
    fi
    echo "Downloading AsyncAPI spec ..."
    curl -f http://localhost:8080/docs/asyncapi -o "$project_path/assets/asyncapi-docs.json"
  fi

  echo "Stopping Spring Boot app for $project_name (pid=$GRADLE_PID) ..."
  kill "$GRADLE_PID" || true
  unset GRADLE_PID
  if ! wait_for_api_to_fail "http://localhost:8080/info"; then
    echo "ERROR: Timed out waiting for Spring Boot app to stop for $project_name"
    exit 1
  fi
done

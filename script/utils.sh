#!/usr/bin/env bash

fail() {
  echo "${1:-Error occurred}"
  exit 1
}

requires() {
  for arg in "$@"; do
    command -v "$arg" >/dev/null 2>&1 || fail "'$arg' is not installed. Please install '$arg' and try again."
  done
}

print_usage() {
  grep '^##' "$1" | cut -c 3-
}

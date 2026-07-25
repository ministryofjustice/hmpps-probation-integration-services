#!/usr/bin/env bash
##
## Copy the GitHub App credentials from the central Kubernetes secret into GitHub Actions and Dependabot secrets for all
## repositories managed by the probation-integration team.
##
set -euo pipefail

NAMESPACE="hmpps-probation-integration"
KUBE_SECRET_NAME="github-app-probation-integration-bot"

GITHUB_ORG="ministryofjustice"
REPOSITORIES=(
  "community-api"
  "court-hearing-event-receiver"
  "crime-portal-gateway"
  "hmpps-appointment-reminders-ui"
  "hmpps-probation-integration-e2e-tests"
  "hmpps-probation-integration-e2e-test-reports"
  "hmpps-probation-integration-services"
  "hmpps-supervision"
  "hmpps-tier"
  "hmpps-tier-ui"
  "ndelius-new-tech"
  "pdf-generator"
  "probation-offender-search"
  "probation-search-frontend"
  "probation-search-ui"
)

echo "Reading Kubernetes secret '${KUBE_SECRET_NAME}' from namespace '${NAMESPACE}'..."

BOT_APP_ID="$(
  kubectl get secret "${KUBE_SECRET_NAME}" \
    --namespace "${NAMESPACE}" \
    --output jsonpath='{.data.APP_ID}' \
  | base64 --decode
)"

BOT_APP_PRIVATE_KEY="$(
  kubectl get secret "${KUBE_SECRET_NAME}" \
    --namespace "${NAMESPACE}" \
    --output jsonpath='{.data.PRIVATE_KEY}' \
  | base64 --decode
)"

if [[ -z "${BOT_APP_ID}" ]]; then
  echo "ERROR: APP_ID was empty or missing from Kubernetes secret '${KUBE_SECRET_NAME}'" >&2
  exit 1
fi

if [[ -z "${BOT_APP_PRIVATE_KEY}" ]]; then
  echo "ERROR: PRIVATE_KEY was empty or missing from Kubernetes secret '${KUBE_SECRET_NAME}'" >&2
  exit 1
fi

for repo in "${REPOSITORIES[@]}"; do
  full_repo="${GITHUB_ORG}/${repo}"

  echo "Updating GitHub Actions secrets for ${full_repo}..."

  printf '%s' "${BOT_APP_ID}" \
    | gh secret set BOT_APP_ID \
      --repo "${full_repo}" \
      --app actions

  printf '%s' "${BOT_APP_PRIVATE_KEY}" \
    | gh secret set BOT_APP_PRIVATE_KEY \
      --repo "${full_repo}" \
      --app actions

  echo "Updating GitHub Dependabot secrets for ${full_repo}..."

  printf '%s' "${BOT_APP_ID}" \
    | gh secret set BOT_APP_ID \
      --repo "${full_repo}" \
      --app dependabot

  printf '%s' "${BOT_APP_PRIVATE_KEY}" \
    | gh secret set BOT_APP_PRIVATE_KEY \
      --repo "${full_repo}" \
      --app dependabot

  echo "Finished ${full_repo}"
done

echo "All GitHub Actions and Dependabot secrets updated."
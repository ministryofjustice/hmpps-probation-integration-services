#!/bin/bash
set -euo pipefail
eval "$(sentry-cli bash-hook --no-environ)"
sentry-cli monitors run -e "$SENTRY_ENVIRONMENT" redrive-dead-letter-queues -- /scripts/redrive.sh
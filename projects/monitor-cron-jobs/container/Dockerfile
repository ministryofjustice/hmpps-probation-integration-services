FROM alpine:3

RUN apk add --no-cache bash curl ca-certificates jq kubectl

RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN curl -sL https://sentry.io/get-cli/ | bash
USER 1000

COPY --chown=appuser:appgroup /scripts /scripts

ENTRYPOINT ["bash"]

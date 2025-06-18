FROM bitnami/kubectl:1.33

USER root
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# hadolint ignore=DL3008
RUN apt-get update && apt-get upgrade -y \
    && apt-get install --no-install-recommends -y curl jq \
    && rm -rf /var/lib/apt/lists/* \
    && curl -sL https://sentry.io/get-cli/ | bash
USER 1000

COPY --chown=app /scripts /scripts

ENTRYPOINT ["bash"]

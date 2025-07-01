FROM ghcr.io/ministryofjustice/hmpps-devops-tools:latest

USER root
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN curl -sL https://sentry.io/get-cli/ | bash
USER 1000

COPY --chown=app /scripts /scripts

ENTRYPOINT ["bash"]

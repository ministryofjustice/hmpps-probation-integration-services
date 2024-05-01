FROM public.ecr.aws/aws-cli/aws-cli:2.15.42

USER root
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# hadolint ignore=DL3033
RUN yum update -y && \
    yum install -y shadow-utils jq && \
    yum clean all && \
    useradd --uid 1000 --create-home --system app && \
    curl -sL https://sentry.io/get-cli/ | bash
USER 1000

COPY --chown=app /scripts /scripts

ENTRYPOINT ["/scripts/startup.sh"]

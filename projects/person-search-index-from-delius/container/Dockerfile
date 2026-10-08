FROM mikefarah/yq:4 AS yml
# Convert yml files to json
COPY --chown=yq /pipelines /pipelines
RUN find /pipelines -type f -name "*.yml" -exec sh -c 'for f; do yq -o=json "$f" > "${f%.yml}.json"; done' sh {} +


FROM elastic/logstash:9.5.5

USER root

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN microdnf install -y "jq-1.6-*" "gettext-0.21-*" \
 && microdnf clean all

RUN curl -sL https://sentry.io/get-cli/ | bash

USER 1000

RUN LS_JAVA_OPTS='-Xmx2G' bin/logstash-plugin install logstash-input-opensearch && \
    LS_JAVA_OPTS='-Xmx2G' bin/logstash-plugin install logstash-output-opensearch

COPY --chown=logstash ojdbc11.jar /etc/logstash/
COPY --chown=logstash config /usr/share/logstash/config
COPY --chown=logstash /scripts /scripts
COPY --chown=logstash --from=yml /pipelines /pipelines
RUN mkdir -p /usr/share/logstash/data/dead_letter_queue/person-incremental \
             /usr/share/logstash/data/dead_letter_queue/person-full-load \
             /usr/share/logstash/data/dead_letter_queue/contact-keyword-incremental \
             /usr/share/logstash/data/dead_letter_queue/contact-keyword-full-load \
             /usr/share/logstash/data/dead_letter_queue/contact-semantic-incremental \
             /usr/share/logstash/data/dead_letter_queue/contact-semantic-full-load

# Workaround for the jdbc_streaming plugin not supporting statement_filepath.
# See https://github.com/logstash-plugins/logstash-integration-jdbc/issues/51
RUN bash /scripts/prepare-pipelines.sh

ENTRYPOINT ["/scripts/startup.sh"]

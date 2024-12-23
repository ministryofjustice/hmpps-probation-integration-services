FROM mikefarah/yq:4 as yml
# Convert yml files to json
COPY --chown=yq /pipelines /pipelines
RUN find /pipelines -type f -name "*.yml" -exec sh -c 'for f; do yq -o=json "$f" > "${f%.yml}.json"; done' sh {} +


FROM logstash:8.17.0

USER root
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# hadolint ignore=DL3008
RUN apt-get update && apt-get upgrade -y \
    && apt-get install --no-install-recommends -y curl jq gettext \
    && rm -rf /var/lib/apt/lists/* \
    && curl -sL https://sentry.io/get-cli/ | bash
USER 1000

RUN bin/logstash-plugin install logstash-input-opensearch && \
    bin/logstash-plugin install logstash-output-opensearch

COPY --chown=logstash ojdbc11.jar /etc/logstash/
COPY --chown=logstash config /usr/share/logstash/config
COPY --chown=logstash /scripts /scripts
COPY --chown=logstash --from=yml /pipelines /pipelines
RUN mkdir -p /usr/share/logstash/data/dead_letter_queue/person-incremental \
             /usr/share/logstash/data/dead_letter_queue/person-full-load \
             /usr/share/logstash/data/dead_letter_queue/contact-incremental \
             /usr/share/logstash/data/dead_letter_queue/contact-full-load

# Workaround for the jdbc_streaming plugin not supporting statement_filepath.
# See https://github.com/logstash-plugins/logstash-integration-jdbc/issues/51
RUN sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(tr '\n' ' ' < /pipelines/person/statement.sql | sed 's/"/\\\\"/g;s/:batch_size/0/g;s/:sql_last_value/0/g')/" /pipelines/person/logstash-incremental.conf \
 && sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(tr '\n' ' ' < /pipelines/contact/statement.sql | sed 's/"/\\\\"/g;s/:batch_size/0/g;s/:sql_last_value/0/g;s/:contact_id/?/g')/" /pipelines/contact/logstash-incremental.conf

# Replace literals with bind parameters
RUN sed -i -E 's/\B:\w+/?/g' /pipelines/contact/statement.sql

ENTRYPOINT ["/scripts/startup.sh"]

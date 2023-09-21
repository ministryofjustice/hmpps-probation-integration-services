FROM mikefarah/yq:4 as yml
# Convert yml files to json
COPY --chown=yq /pipelines /pipelines
RUN find /pipelines -type f -name '*.yml' -exec sh -c 'f="$1"; yq -o=json "$f" > "${f%.yml}.json"' shell {} +;


FROM logstash:8.10.1

USER root
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# hadolint ignore=DL3008
RUN apt-get update && apt-get upgrade -y \
    && apt-get install -y curl jq \
    && rm -rf /var/lib/apt/lists/* \
    && curl -sL https://sentry.io/get-cli/ | bash
USER 1000

COPY --chown=logstash ojdbc11.jar /etc/logstash/
COPY --chown=logstash config /usr/share/logstash/config
COPY --chown=logstash /scripts /scripts
COPY --chown=logstash --from=yml /pipelines /pipelines

RUN bin/logstash-plugin install logstash-input-opensearch && \
    bin/logstash-plugin install logstash-output-opensearch && \
    mkdir -p /usr/share/logstash/data/dead_letter_queue/person-incremental \
             /usr/share/logstash/data/dead_letter_queue/person-full-load \
             /usr/share/logstash/data/dead_letter_queue/contact-incremental \
             /usr/share/logstash/data/dead_letter_queue/contact-full-load

# Workaround for the jdbc_streaming plugin not supporting statement_filepath.
# See https://github.com/logstash-plugins/logstash-integration-jdbc/issues/51
RUN sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(tr '\n' ' ' < /pipelines/person/statement.sql | sed 's/"/\\\\"/g;s/:batch_size/0/g;s/:sql_last_value/0/g')/" /pipelines/person/logstash-incremental.conf
RUN sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(tr '\n' ' ' < /pipelines/contact/statement.sql | sed 's/"/\\\\"/g;s/:batch_size/0/g;s/:sql_last_value/0/g')/" /pipelines/contact/logstash-incremental.conf

ENTRYPOINT ["/scripts/startup.sh"]

FROM mikefarah/yq:4 AS yml
# Convert yml files to json
COPY --chown=yq /pipelines /pipelines
RUN find /pipelines -type f -name "*.yml" -exec sh -c 'for f; do yq -o=json "$f" > "${f%.yml}.json"; done' sh {} +


FROM elastic/logstash:9.5.5

USER root

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN microdnf install -y jq-1.6-19.el9_8.2 gettext-0.21-8.el9 \
 && curl -sL https://sentry.io/get-cli/ | bash \
 && microdnf clean all


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
RUN sed -i "s@\${INCREMENTAL_STATEMENT_SQL}@$(tr '\n' ' ' < /pipelines/person/statement.sql | sed 's/"/\\\\"/g;s/:batch_size/0/g;s/:sql_last_value/0/g;s/:offender_id/?/g')@" /pipelines/person/logstash-incremental.conf \
 && sed -i "s@\${INCREMENTAL_STATEMENT_SQL}@$(tr '\n' ' ' < /pipelines/contact-keyword/statement.sql | sed 's/"/\\\\"/g;s/:batch_size/0/g;s/:sql_last_value/0/g;s/:full_load_contact_threshold/-1/g;s/:contact_id/?/g')@" /pipelines/contact-keyword/logstash-incremental.conf \
 && sed -i "s@\${INCREMENTAL_STATEMENT_SQL}@$(tr '\n' ' ' < /pipelines/contact-semantic/statement.sql | sed 's/"/\\\\"/g;s/:batch_size/0/g;s/:sql_last_value/0/g;s/:full_load_contact_threshold/-1/g;s/:contact_id/?/g')@" /pipelines/contact-semantic/logstash-incremental.conf

# Replace literals with bind parameters
RUN sed -i -E 's/\B:\w+/?/g' /pipelines/person/statement.sql \
 && sed -i -E 's/\B:\w+/?/g' /pipelines/contact-keyword/statement.sql \
 && sed -i -E 's/\B:\w+/?/g' /pipelines/contact-semantic/statement.sql

FROM opensearchproject/logstash-oss-with-opensearch-output-plugin:7.16.3

COPY --chown=logstash ojdbc11.jar statement.sql /etc/logstash/
COPY --chown=logstash logstash-full-load.conf /usr/share/logstash/full-load/logstash.conf
COPY --chown=logstash logstash-full-load-dlq.conf /usr/share/logstash/full-load-dlq/logstash.conf
COPY --chown=logstash logstash-incremental.conf /usr/share/logstash/incremental/logstash.conf
COPY --chown=logstash logstash-incremental-dlq.conf /usr/share/logstash/incremental-dlq/logstash.conf
COPY --chown=logstash logstash.yml pipelines.yml /usr/share/logstash/config/
RUN mkdir -p /usr/share/logstash/data/dead_letter_queue/incremental /usr/share/logstash/data/dead_letter_queue/full-load

# Workaround for the jdbc_streaming plugin not supporting statement_filepath.
# See https://github.com/logstash-plugins/logstash-integration-jdbc/issues/51
RUN sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(cat /etc/logstash/statement.sql | tr '\n' ' ' | sed 's/"/\\\\"/g')/" /usr/share/logstash/incremental/logstash.conf

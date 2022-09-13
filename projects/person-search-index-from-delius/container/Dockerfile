FROM opensearchproject/logstash-oss-with-opensearch-output-plugin:7.16.3

COPY --chown=logstash ojdbc11.jar statement.sql /etc/logstash/
COPY --chown=logstash logstash-full-load.conf /usr/share/logstash/full-load/logstash.conf
COPY --chown=logstash logstash-full-load-dlq.conf /usr/share/logstash/full-load-dlq/logstash.conf
COPY --chown=logstash logstash-incremental.conf /usr/share/logstash/incremental/logstash.conf
COPY --chown=logstash logstash-incremental-dlq.conf /usr/share/logstash/incremental-dlq/logstash.conf
COPY --chown=logstash logstash.yml pipelines.yml /usr/share/logstash/config/

COPY --chown=logstash management/person-search-pipeline.json /tmp/init/
COPY --chown=logstash management/person-search-template.json /tmp/init/
COPY --chown=logstash management/init-script.sh /tmp/init/

COPY --chown=logstash management/index-alias-management.sh /tmp/

USER root
RUN apt-get update && apt-get -y install cron && apt-get -y install jq
RUN chmod u+s /usr/sbin/cron

USER logstash

RUN mkdir -p /usr/share/logstash/data/dead_letter_queue/incremental /usr/share/logstash/data/dead_letter_queue/full-load

# Workaround for the jdbc_streaming plugin not supporting statement_filepath.
# See https://github.com/logstash-plugins/logstash-integration-jdbc/issues/51
RUN sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(cat /etc/logstash/statement.sql | tr '\n' ' ' | sed 's/"/\\\\"/g')/" /usr/share/logstash/incremental/logstash.conf

ENTRYPOINT /tmp/init/init-script.sh -p /tmp/init/person-search-pipeline.json -t /tmp/init/person-search-template.json \
            && echo "${MANAGMENT_SCHEDULE} /tmp/index-alias-management.sh -u ${SEARCH_INDEX_HOST} >> /tmp/cron.log 2>&1" | crontab - \
            && cron \
            && /opt/logstash/bin/logstash
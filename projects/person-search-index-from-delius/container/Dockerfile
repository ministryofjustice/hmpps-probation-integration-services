FROM ghcr.io/ministryofjustice/hmpps-probation-integration-services/opensearch-logstash:8

COPY --chown=logstash ojdbc11.jar statement.sql /etc/logstash/
COPY --chown=logstash logstash-full-load.conf /usr/share/logstash/full-load/logstash.conf
COPY --chown=logstash logstash-full-load-dlq.conf /usr/share/logstash/full-load-dlq/logstash.conf
COPY --chown=logstash logstash-incremental.conf /usr/share/logstash/incremental/logstash.conf
COPY --chown=logstash logstash-incremental-dlq.conf /usr/share/logstash/incremental-dlq/logstash.conf
COPY --chown=logstash logstash.yml pipelines.yml /usr/share/logstash/config/

COPY --chown=logstash management/* /search-management/

USER root
RUN apt-get update && apt-get -y install \
  cron \
  jq \
  && rm -rf /var/lib/apt/lists/* \
  && chmod u+s /usr/sbin/cron \
  && crontab -u logstash /etc/cron.d/restart-cron \
  && usermod -a -G crontab logstash

USER 1000

RUN mkdir -p /usr/share/logstash/data/dead_letter_queue/incremental /usr/share/logstash/data/dead_letter_queue/full-load

# Workaround for the jdbc_streaming plugin not supporting statement_filepath.
# See https://github.com/logstash-plugins/logstash-integration-jdbc/issues/51
RUN sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(cat /etc/logstash/statement.sql | tr '\n' ' ' | sed 's/"/\\\\"/g')/" /usr/share/logstash/incremental/logstash.conf

ENTRYPOINT /search-management/init-script.sh -p /search-management/person-search-pipeline.json -t /search-management/person-search-template.json \
            && echo "${MANAGEMENT_SCHEDULE} SEARCH_INDEX_HOST=${SEARCH_INDEX_HOST} APPLICATIONINSIGHTS_CONNECTION_STRING='${APPLICATIONINSIGHTS_CONNECTION_STRING}'  /search-management/index-alias-management.sh >> /tmp/cron.log 2>&1" | crontab - \
            && cron \
            && tail --retry -F /tmp/cron.log & /usr/local/bin/docker-entrypoint
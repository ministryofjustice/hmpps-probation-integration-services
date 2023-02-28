FROM mikefarah/yq:4 as yml
# Convert yml files to json
COPY --chown=yq /init /init
RUN find /init -type f -name '*.yml' | xargs -n1 -I{} sh -c 'f={}; yq -o=json "$f" > "${f%.yml}.json"'


FROM opensearchproject/logstash-oss-with-opensearch-output-plugin:8.6.1

USER root
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*
USER 1000

COPY --chown=logstash ojdbc11.jar /etc/logstash/
COPY --chown=logstash config /usr/share/logstash/config
COPY --chown=logstash pipelines /pipelines
COPY --chown=logstash --from=yml /init /init

RUN mkdir -p /usr/share/logstash/data/dead_letter_queue/person-incremental \
             /usr/share/logstash/data/dead_letter_queue/person-full-load \
             /usr/share/logstash/data/dead_letter_queue/contact-incremental \
             /usr/share/logstash/data/dead_letter_queue/contact-full-load

# Workaround for the jdbc_streaming plugin not supporting statement_filepath.
# See https://github.com/logstash-plugins/logstash-integration-jdbc/issues/51
RUN sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(cat /pipelines/person/statement.sql | tr '\n' ' ' | sed 's/"/\\\\"/g;s/:offset/0/g;s/:size/1/g;s/:sql_last_value/0/g')/" /pipelines/person/logstash-incremental.conf
RUN sed -i "s/\${INCREMENTAL_STATEMENT_SQL}/$(cat /pipelines/contact/statement.sql | tr '\n' ' ' | sed 's/"/\\\\"/g;s/:offset/0/g;s/:size/1/g;s/:sql_last_value/0/g')/" /pipelines/contact/logstash-incremental.conf

ENTRYPOINT /init/setup-index.sh -i "$PERSON_INDEX_PREFIX" -p /init/person-search-pipeline.json -t /init/person-search-template.json \
        && /init/setup-index.sh -i "$CONTACT_INDEX_PREFIX" -t /init/contact-search-template.json \
        && /usr/local/bin/docker-entrypoint

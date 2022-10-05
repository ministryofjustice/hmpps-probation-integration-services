FROM opensearchproject/logstash-oss-with-opensearch-output-plugin:8.4.0
USER root
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*
USER logstash
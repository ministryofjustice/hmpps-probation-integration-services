FROM docker.elastic.co/logstash/logstash-oss:7.9.1

COPY logstash.conf /usr/share/logstash/pipeline/
COPY ojdbc11.jar /etc/logstash/
COPY statement.sql /etc/logstash


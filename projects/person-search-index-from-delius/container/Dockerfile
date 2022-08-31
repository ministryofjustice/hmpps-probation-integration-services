FROM docker.elastic.co/logstash/logstash-oss:7.9.1

COPY logstash.conf /usr/share/logstash/pipeline/
COPY logstash.yml /usr/share/logstash/config/
COPY ojdbc11.jar /etc/logstash/
COPY statement.sql /etc/logstash/

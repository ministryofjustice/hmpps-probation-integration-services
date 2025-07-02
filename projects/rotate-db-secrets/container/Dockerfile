FROM bitnami/kubectl:1.33

USER root
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# hadolint ignore=DL3008
ENV LD_LIBRARY_PATH=/opt/oracle/instantclient_19_24:$LD_LIBRARY_PATH
ENV PATH=/opt/oracle/instantclient_19_24:$PATH
RUN apt update -y && \
    apt upgrade -y && \
    apt install -y curl \
    libaio1 \
    unzip
RUN mkdir -p /opt/oracle
WORKDIR /opt/oracle
RUN curl -o oracleclient.zip https://download.oracle.com/otn_software/linux/instantclient/1924000/instantclient-basic-linux.x64-19.24.0.0.0dbru.zip && \
    curl -o sqlplus.zip https://download.oracle.com/otn_software/linux/instantclient/1924000/instantclient-sqlplus-linux.x64-19.24.0.0.0dbru.zip && \
    curl -o oracletools.zip https://download.oracle.com/otn_software/linux/instantclient/1924000/instantclient-tools-linux.x64-19.24.0.0.0dbru.zip && \
    unzip oracleclient.zip && \
    unzip oracletools.zip && \
    unzip sqlplus.zip && \
    sh -c "echo /opt/oracle/instantclient_19_24 > \
    /etc/ld.so.conf.d/oracle-instantclient.conf" && \
    ldconfig


USER 1003

COPY --chown=app /scripts /scripts

ENTRYPOINT ["bash"]

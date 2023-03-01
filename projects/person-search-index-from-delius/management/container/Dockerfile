FROM alpine
RUN adduser -D -u 1000 appuser && \
    apk --no-cache add curl bash jq
COPY --chown=appuser *.sh /scripts/
USER 1000
WORKDIR /scripts
ENTRYPOINT ["/bin/bash"]
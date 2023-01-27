FROM alpine
COPY index-alias-management.sh /scripts/
RUN adduser -D -u 1000 appuser && \
    apk --no-cache add curl bash jq
USER 1000
ENTRYPOINT ["/bin/bash"]
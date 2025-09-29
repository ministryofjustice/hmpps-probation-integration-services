FROM ghcr.io/flipt-io/flipt:v2.1.3
USER root
RUN apk upgrade --no-cache
USER flipt

# Run any pending migrations on startup
CMD ["sh", "-c", "./flipt migrate && ./flipt"]
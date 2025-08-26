FROM ghcr.io/flipt-io/flipt:v2.0.1
USER root
RUN apk upgrade --no-cache
USER flipt

# Run any pending migrations on startup
CMD ["sh", "-c", "./flipt migrate && ./flipt"]
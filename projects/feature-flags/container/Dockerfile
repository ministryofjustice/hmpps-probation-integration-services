FROM ghcr.io/flipt-io/flipt:v1.54.2

# Run any pending migrations on startup
CMD ["sh", "-c", "./flipt migrate && ./flipt"]
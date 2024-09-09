FROM ghcr.io/flipt-io/flipt:v1.49.1

# Run any pending migrations on startup
CMD ["sh", "-c", "./flipt migrate && ./flipt"]
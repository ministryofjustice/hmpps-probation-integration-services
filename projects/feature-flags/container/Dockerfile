FROM ghcr.io/flipt-io/flipt:v1.50.1

# Run any pending migrations on startup
CMD ["sh", "-c", "./flipt migrate && ./flipt"]
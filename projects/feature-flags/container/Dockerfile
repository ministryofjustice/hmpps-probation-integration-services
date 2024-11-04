FROM ghcr.io/flipt-io/flipt:v1.51.0

# Run any pending migrations on startup
CMD ["sh", "-c", "./flipt migrate && ./flipt"]
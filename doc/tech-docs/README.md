# Technical Documentation Site

https://ministryofjustice.github.io/hmpps-probation-integration-services/tech-docs

# Developing

## Requirements

- Docker

## Previewing locally

Running the following command will run your technical documentation site locally using [ministryofjustice/tech-docs-github-pages-publisher](https://github.com/ministryofjustice/tech-docs-github-pages-publisher), allowing you to access it by visiting <http://127.0.0.1:4567> in your browser

```bash
make preview
```

## Checking links locally

This repository includes a GitHub Actions workflow that uses [Lychee](https://github.com/lycheeverse/lychee) for checking links.

To perform this locally, you will either need to use the dev container or install Lychee, and the run:

```bash
make link-check
```

# Publishing

There is a GitHub Actions workflow ([`.github/workflows/docs.yml`](/.github/workflows/docs.yml)) for publishing to GitHub Pages when merging to `main`.

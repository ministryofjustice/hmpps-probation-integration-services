# Breach Notice and Delius

The [HMPPS Breach Notice service](https://github.com/ministryofjustice/hmpps-breach-notice-ui) enables probation
practitioners to create breach notice letters for offenders who have failed to comply with the requirements of their
community order.

## Business Need

This integration service provides an API for read-only access to case data from Delius, to reduce the need for re-keying
in the Breach Notice service.

It also accepts inbound events when a breach notice is created or deleted, to copy a snapshot of the document into
Delius and Alfresco.

## Context Map

![Context Map](../../doc/tech-docs/source/images/breach-notice-and-delius-context-map.svg)

## Interfaces

### Message Formats

The service responds to HMPPS Offender Event messages via an
[SQS Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/breach-notice-and-delius-queue.tf).

Example [messages](./src/dev/resources/messages/) are in the development source tree

## Event Triggers

| Business Event          | Message Event Type / Filter          |
|-------------------------|--------------------------------------|
| Breach notice completed | probation-case.breach-notice.created |
| Breach notice deleted   | probation-case.breach-notice.deleted |

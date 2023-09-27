# Manage Offences and Delius

## Business Need

Inbound service that responds to events raised by the HMPPS Manage Offences service when there are changes to the set of [CJS Offence Codes](https://www.criminaljusticehub.org.uk/jargon-buster/cjs-offence-code/) managed by that service. Any changes to the set of offence codes is reflected in the Delius database. This ensures that Delius is up-to-date with the common set of offence codes used across HMPPS Digital services.

## Context Map

![Context Map](tech-docs/source/img/manage-offences-and-delius-context-map.svg)

## Interfaces

### Message Formats

The service responds to an HMPPS Domain Event message via an
[SQS Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/manage-offences-and-delius-queue.tf).
The events are raised by the [Manage Offences API](https://github.com/ministryofjustice/hmpps-manage-offences-api) when a CJS offence code is either created or updated.

| Business Event             | Message Class      | Message Event Type / Filter     |
|----------------------------|--------------------|---------------------------------|
| Change to CJS offence code | HMPPS Domain Event | manage-offences.offence.changed |

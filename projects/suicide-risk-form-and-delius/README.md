# Suicide-risk-form-and-delius

The [HMPPS Suicide Risk Form service](https://github.com/ministryofjustice/hmpps-suicide-risk-form-ui) enables probation
practitioners to create breach suicide risk forms for offenders who may be at risk of suicide.

## Business Need

This integration service provides an API for read-only access to case data from Delius, to reduce the need for re-keying
in the Sucide Risk Form service, and publishes a domain event when a new form is created or a form is deleted.

## Context Maps

:TODO add link to context maps

## Interfaces

### Message Formats

The service publishes domain events to
[SQS Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/suicide-risk-form-and-delius-queue.tf).

Example [messages](./src/dev/resources/messages/) are in the development source tree

## Authorisation

| API Endpoint | Required Role                                 |
|--------------|-----------------------------------------------|
| /            | PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL |
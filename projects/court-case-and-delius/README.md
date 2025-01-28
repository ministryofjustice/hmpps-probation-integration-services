# Court Case and Delius

This service listens for domain events containing case notes. The notes are merged with an existing case note or a new case note is created if one does not exist. If a case note arrives out of order, a warning will be logged and it will be ignored.

This service also provides a read-only API for authorised users to get information about a **Court case** and the **Conviction**.

# Business need

The consumer is used to update the Delius system when a case note is published for a Court case. The Delius system is used to inform practitioners, who manage cases, when there has been an update to the case notes.

The service also provides an API which is used by the Prepare a Case for Sentence service to get Court case details.

# Data dependencies
This service enables adding or updating data for case notes (contact entity in Delius) only. It provides read-only access relating to a **Person on Probation**, the **Event** (convictions, offences and Court appearances outcomes) and the **Court case** details.

## Context Map - Court Case data

![Context Map](./tech-docs/source/img/court-case-context-map.svg)

# Workflows
## Court case notes submitted
The notes are merged with an existing case note or a new case note is created if one does not exist. If a case note arrives out of order, a warning will be logged and it will be ignored.

| Business Event            | Message Event Type / Filter  |
| ------------------------- | ---------------------------- |
| Court case note published | court-case.comment.published |

![Context Map](./tech-docs/source/img/court-case-notes-submitted-workflow.svg)

# Interface
## Message formats

The service responds to HMPPS Domain Event messages via the
[Court Case and Delius Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/court-case-and-delius-queue.tf).
The events are raised by TODO.

Example [messages](./src/dev/resources/messages/) are in the development source tree.

Incoming messages are filtered on `eventType` by the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/court-case-and-delius-queue.tf)

# API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint           | Required Role                             |
| ---------------------- | ----------------------------------------- |
| /secure/offenders/     | PROBATION_API_\_COURT_CASE_\_CASE_DETAILS |
| /probation-case/{urn}/ | PROBATION_API_\_COURT_CASE_\_CASE_DETAILS |
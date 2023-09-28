# Prison Case Notes to Probation

Inbound service that responds to case note creation events, calls the [Offender
Case Notes Service](https://github.com/ministryofjustice/offender-case-notes)
and adds the relevant information to the Delius case history as a contact.

## Probation Business Need

Prison case notes are displayed as contacts in the NDelius contact log and
used by probation practitioners as part of the probation case history

## Context Map

![Context Map](tech-docs/source/img/prison-case-notes-to-probation-context-map.svg)

## Interfaces

### Message Formats

The service responds to 'offender events' messages via the
[Case Notes SQS Sub Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/offender-events-dev/resources/case-notes-sub-queue.tf)
which are raised by the [Prison Offender Events](https://github.com/ministryofjustice/prison-offender-events)
service

Example [messages](./src/dev/resources/messages/) are in the development source tree

Incoming messages are filtered on `eventType` by the [SQS queue
policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/offender-events-dev/resources/case-notes-sub-queue.tf#L120-L135)

### Case Note Details

The service uses the case note id contained in the message and a known URL for
the [Offender Case Notes Service](https://github.com/ministryofjustice/offender-case-notes)
to request the specific case note details.

### Person Matching

Person records are matched across NOMIS and Delius using the NOMS number. This
relies on the NOMS number being set in Delius which happens as part of the
processing within the [Prison to Probation Update](https://github.com/ministryofjustice/prison-to-probation-update)
service

## Event Triggers

Prison case notes are collected and transferred based on fine-grained data
events raised when there are changes to the NOMIS database tables. As a result
of this the case notes do not necessarily map to a single business activity.
The events we respond to are roughly raised in the following circumstances:

| Business Event                   | Message Class         | Message Event Type / Filter |
|----------------------------------|-----------------------|-----------------------------|
| Person Released from Institution | Prison Offender Event | "PRISON-RELEASE"            |
| Person Transferred               | Prison Offender Event | "TRANSFER-FROMTOL"          |
| General Observations             | Prison Offender Event | "GEN-OSE"                   |
| Alerts Active                    | Prison Offender Event | "ALERT-ACTIVE"              |
| Alerts Inactive                  | Prison Offender Event | "ALERT-INACTIVE"            |
| All OMiC Events                  | Prison Offender Event | { prefix = "OMIC" }         |
| All OMiC OPD Events              | Prison Offender Event | { prefix = "OMIC_OPD" }     |
| Keyworking Events                | Prison Offender Event | { prefix = "KA" }           |

## HMPPS Technical Environment

This service takes on the responsibilities of the existing [Case Notes to Probation](https://github.com/ministryofjustice/case-notes-to-probation)
service. It also supports the deprecation of the NDelius HTTP/JSON API interface for
ingesting case notes and the [Community API](https://github.com/ministryofjustice/community-api)
endpoint (`PUT /secure/nomisCaseNotes/{nomisId}/{caseNotesId}`) that calls this.

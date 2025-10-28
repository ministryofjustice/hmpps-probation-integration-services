# Prison Case Notes to Probation

Inbound service that responds to case note and prison alert creation events, calls the [Offender
Case Notes Service](https://github.com/ministryofjustice/offender-case-notes)
the [Prison Alerts Service](https://github.com/ministryofjustice/hmpps-alerts-api) respectively,
and adds the relevant information to the Delius case history as a contact.

## Probation Business Need

Prison case notes and alerts are displayed as contacts in the NDelius contact log and
used by probation practitioners as part of the probation case history

## Context Map

![Context Map](../../doc/tech-docs/source/images/prison-case-notes-to-probation-context-map.svg)

## Interfaces

### Message Formats

The service responds to 'HMPPS domain events' messages via the
[SQS queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-dev/resources/prison-case-notes-to-probation-queue.tf)
which are raised by the [Prison Offender Events](https://github.com/ministryofjustice/prison-offender-events)
service

Example [messages](./src/dev/resources/messages/) are in the development source tree

Incoming messages are filtered on `eventType` by
the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-dev/resources/prison-case-notes-to-probation-queue.tf)

### Case Note Details

The service uses the case note id contained in the message and a known URL for
the [Offender Case Notes Service](https://github.com/ministryofjustice/offender-case-notes)
to request the specific case note details.

### Alert Details

The service uses the `detailUrl` contained in the message to request the specific
alert details from the [Prison Alerts Service](https://github.com/ministryofjustice/hmpps-alerts-api).

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

| Business Event                      | Message Class      | Message Event Type / Filter              | Case note type filter   |
|-------------------------------------|--------------------|------------------------------------------|-------------------------|
| NOMIS ID linked to a probation case | HMPPS Domain Event | "probation-case.prison-identifier.added" | All below types         |
| Person Released from Institution    | HMPPS Domain Event | "person.case-note.created"/"updated"     | "PRISON-RELEASE"        |
| Person Transferred                  | HMPPS Domain Event | "person.case-note.created"/"updated"     | "TRANSFER-FROMTOL"      |
| General Observations                | HMPPS Domain Event | "person.case-note.created"/"updated"     | "GEN-OSE"               |
| Alerts Active                       | HMPPS Domain Event | "person.alert.created"/"updated"         | "ALERT-ACTIVE"          |
| Alerts Inactive                     | HMPPS Domain Event | "person.alert.inactive"                  | "ALERT-INACTIVE"        |
| All OMiC Events                     | HMPPS Domain Event | "person.case-note.created"/"updated"     | { prefix = "OMIC" }     |
| All OMiC OPD Events                 | HMPPS Domain Event | "person.case-note.created"/"updated"     | { prefix = "OMIC_OPD" } |
| Keyworking Events                   | HMPPS Domain Event | "person.case-note.created"/"updated"     | { prefix = "KA" }       |

## HMPPS Technical Environment

This service takes on the responsibilities of the existing [Case Notes to Probation](https://github.com/ministryofjustice/case-notes-to-probation)
service. It also supports the deprecation of the NDelius HTTP/JSON API interface for
ingesting case notes and the [Community API](https://github.com/ministryofjustice/community-api)
endpoint (`PUT /secure/nomisCaseNotes/{nomisId}/{caseNotesId}`) that calls this.

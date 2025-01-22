# Assessment Summary and Delius

This service listens to domain events when a new assessment has been produced in the OASys system. The assessment is conducted for a person who has been sentenced to assess their needs and risks. Delius will record a summary of the assessment.

When any new risks are identified or a change to an existing risk level, a new registration is generated. Existing registrations which are no longer required or considered to be a low risk are recorded as deregistrations.

The risks are categorised as:

* Risk of serious harm (RoSH)
* Other risks

**Note:** The service cannot infer all types of risk from the assessment summary. Some risk registrations are manually added and maintained in Delius.

# Business need

The users typically log into Delius on a regular basis so the assessment summaries and registrations provide a quick overview of the person and risk level. This reduces the need of users having to log into a second system (OASys).


# Data dependencies

Assessment summary relies on the OASys ORDS API for retrieving the assessment summary and RoSH summary. The summary data is compared against the risk levels configured within the Delius database, and the result is used to updated the registration data. The service relies on Delius data for up to date information on a person's latest assessment and active registrations.

## Context Map - Assessment Summary Data

![](./tech-docs/source/img/assessment-summary-context-map.svg)

# Workflows

## Assessment summary produced workflow
The assessment summary is recorded in Delius. The risks are evaluated and stored as registrations.

| Business Event        | Message Event Type / Filter  |
| --------------------- | ---------------------------- |
| Assessment submitted  | assessment.summary.produced  |

### Record assessment
The previous assessment summary is deleted and replaced with the new assessment summary. The full assessment can be retrieved from OASys.

![](./tech-docs/source/img/assessment-summary-assessment-submitted.svg)

### Record risks
The registrations for risk of serious harm (RoSH) and other risks will be checked. Any risks which are no longer required or low risks will result in a deregistration being created. Registrations will be added or updated for active risks.

**Note:** The service cannot infer all types of risk. Some risk registrations are manually added and maintained in Delius.

#### Risk of serious harm

![](./tech-docs/source/img/assessment-summary-record-rosh.svg)


#### Other risks

![](./tech-docs/source/img/assessment-summary-record-other-risks.svg)

# Interfaces

## Message formats

The service responds to HMPPS Domain Event messages via the [Assessment Summary and Delius Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/assessment-summary-and-delius-queue.tf). The events are raised by OASys to communicate the creation of an assessment of a person.

Example [messages](./src/dev/resources/messages/) are in the development source tree.

Incoming messages are filtered on `eventType` by the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/assessment-summary-and-delius-queue.tf)
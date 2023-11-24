# Approved Premises and Delius

Service that responds to Approved Premises domain events, calls the
[HMPPS Approved Premises API](https://github.com/ministryofjustice/hmpps-approved-premises-api)
and records referral progress against the probation case record in Delius. We
also provide an API to give probation case information to support making
approved premises referrals.

## Business Need

Supporting the Approved Premises service with the correct information required
to make an Approved Premises application and ensuring the Probation
Practitioner has an up-to-date view of activity in the Approved Premises
service when interacting with the probation case via Delius.

## Data Dependencies

_Approved Premises (CAS1)_ depends on _Delius_ data for background on the relevant **Person on Probation** and the specific **Probation Case** when making an approved premises referral. There is also a dependency on **Probation Practitioner** information and **Probation Documents** used in the referral process.

### Context Map

![Context Map](./tech-docs/source/img/approved-premises-and-delius-context-map.svg)

## Workflows

Approved Premises domain events are raised in real time as approved premises
referrals are processed. The progressive stages of a referral are raised as
separate events.

### Approved Premises Referral Application Workflows

_Approved Premises (CAS1)_ referral activity is reflected in _Delius_ via simple notification contacts, intended to log updates to the case and inform probation practitioners of changes.

| Business Event                                        | HMPPS Domain Event Type                 |
|-------------------------------------------------------|-----------------------------------------|
| A referral to an approved premises has been assessed  | approved-premises.application.assessed  |
| A referral to an approved premises has been submitted | approved-premises.application.submitted |
| A referral to an approved premises has been withdrawn | approved-premises.application.withdrawn |

![Workflow Map](./tech-docs/source/img/approved-premises-and-delius-workflow-application.svg)


### Approved Premises Booking Workflows

_Approved Premises (CAS1)_ bookings are reflected in _Delius_ via the entries in the `APPROVED_PREMISES` database table, which appear in the _Delius_ UI as the **Approved Premises Diary** and **Approved Premises Referrals** screens. The information held in Delius is kept up-to-date with the activity in the _Approved Premises CAS1_ service via processing of specific domain events. The information is duplicated in _Delius_ for the purposes of maintaining MIS reports that depend on certain aspects of the data related to arrival and departure dates.

#### Booking Made Workflow

| Business Event                                  | HMPPS Domain Event Type             |
|-------------------------------------------------|-------------------------------------|
| An approved premises booking has been completed | approved-premises.booking.made      |

![Workflow Map](./tech-docs/source/img/approved-premises-and-delius-workflow-booking-made.svg)

#### Booking Changed Workflow

| Business Event                                  | HMPPS Domain Event Type             |
|-------------------------------------------------|-------------------------------------|
| An approved premises booking has been changed   | approved-premises.booking.changed   |

![Workflow Map](./tech-docs/source/img/approved-premises-and-delius-workflow-booking-changed.svg)

#### Booking Cancelled Workflow

| Business Event                                  | HMPPS Domain Event Type             |
|-------------------------------------------------|-------------------------------------|
| An approved premises booking has been cancelled | approved-premises.booking.cancelled |

![Workflow Map](./tech-docs/source/img/approved-premises-and-delius-workflow-booking-cancelled.svg)

## Interfaces

### Message Formats

The service responds to various HMPPS Domain Event message via the
[Approved Premises and Delius Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/approved-premises-and-delius-queue.tf).
The events are raised by the [HMPPS Approved Premises Service](https://github.com/ministryofjustice/hmpps-approved-premises-api)
to communicate important events in the approved premises referrals process.

Example [messages](./src/dev/resources/messages/) are in the development source tree.

Incoming messages are filtered on `eventType` by the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/cc44e15d883b04d1caf5663eec6025674dc10eb5/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/approved-premises-and-delius-queue.tf#L5-L14)

## Authorisation

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint                    | Required Role                   |
|---------------------------------|---------------------------------|
| /approved-premises/{code}/staff | ROLE\_APPROVED\_PREMISES\_STAFF |
| /teams/managingCase/{crn}       | ROLE\_APPROVED\_PREMISES\_STAFF |

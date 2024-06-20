# CAS2 and Delius

Service that responds to Community Accommodation Services Tier 2 (CAS2) domain events. The service is responsible for updating Delius with progress of referrals to short-term accommodation for people released from custody on Home Detention Curfew or bail.

## Business Need

Ensuring the Probation Practitioner has an up-to-date view of activity in the CAS2 service when interacting with the probation case via Delius.

## Context Map

![Context Map](./tech-docs/src/img/cas2-and-delius-context-map.svg)

## Workflows

CAS2 domain events are raised in real time as referral applications are processed. There is a single update domain event to inform of progress or changes to the referral application.

### Create a CAS2 Referral Application

A referral application is created in the CAS2 service which is reflected in a Delius contact. An `EXTERNAL_REFERENCE` URN is added to the contact in the form `urn:hmpps:cas2:application-submitted:{referralId}` using the CAS2 referral ID as a unique reference.

| Business Event                                                | Message Event Type / Filter                  |
|---------------------------------------------------------------|----------------------------------------------|
| Submission of a accommodation referral application            | applications.cas2.application.submitted      |

![Context Map](./tech-docs/src/img/cas2-and-delius-workflow-application.svg)

### Update the Status of a CAS2 Referral Application

A referral application is updated in the CAS2 service which is reflected in a Delius contact. Only a single update contact exists in Delius for any single CAS2 referral and the contact is updated with the latest status. An `EXTERNAL_REFERENCE` URN is added to the contact in the form `urn:hmpps:cas2:application-status-updated:{referralId}` using the CAS2 referral ID as a unique reference.

| Business Event                                                | Message Event Type / Filter                  |
|---------------------------------------------------------------|----------------------------------------------|
| Update to the status of an accommodation referral application | applications.cas2.application.status-updated |

![Context Map](./tech-docs/src/img/cas2-and-delius-workflow-application-update.svg)

## Interfaces

### Message Interface

The service responds to HMPPS Domain Event messages via the
[Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/cas2-and-delius-queue.tf).
The events are raised by the [HMPPS Approved Premises API](https://github.com/ministryofjustice/hmpps-approved-premises-api/) which handles backend integration for the [CAS2 UI](https://github.com/ministryofjustice/hmpps-community-accommodation-tier-2-ui)

Example [messages](./src/dev/resources/messages/) are in the development source tree.

Incoming messages are filtered on `eventType` by the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/2aeb8aeb7b7798cbe12bc81b14d01aaa707041f1/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/cas2-and-delius-queue.tf#L5-L10)

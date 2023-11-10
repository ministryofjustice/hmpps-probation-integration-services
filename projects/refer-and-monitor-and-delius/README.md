# Refer and Monitor and Delius

## Business Need

_Refer and Monitor an Intervention_ has a tightly coupled relationship with _Delius_ due to activity managed in both systems being important for workflows in the other. _Refer and Monitor an Intervention_ depends on information managed in _Delius_ for it's primary function of creating and managing CRS referrals. There is also a requirement to write information back to _Delius_ when various activities are recorded in _Refer and Monitor an Intervention_. Where possible the interaction between the two services is managed asynchronously via domain events but there are workflows in _Refer and Monitor an Intervention_ that have runtime dependency on successful creation of entities in _Delius_. In these cases the cross-service interaction is via synchronous API calls.

## Data Dependencies

_Refer and Monitor an Intervention_ depends on _Delius_ data for background on the relevant **Person on Probation** and the specific **Probation Case** when making CRS referral. There is also a dependency on **Probation Practitioner** information and **Probation Estate** information when making CRS session appointments

### Context Map

![Context Map](./tech-docs/source/img/randm-and-delius-dependencies-context-map.svg)

## Workflows

### CRS Referral Workflows

CRS referrals are tracked in _Delius_ using an NSI. The request to create the _Delius_ NSI is made as part of the referral creation process in _Refer and Monitor an Intervention_, and the NSI is linked to the referral by adding the _Refer and Monitor an Intervention_ `REFERRAL.ID` to the `NSI.EXTERNAL_REFERENCE` field of the _Delius_ database. The status of the NSI is updated as the CRS referral is progressed in _Refer and Monitor an Intervention_.

#### Context Map - Referrals

![Context Map](./img/randm-and-delius-referral-context-map.svg)

#### Workflow: Create CRS Referral

Creation of the CRS referral is a synchronous integration between _Refer and Monitor an Intervention_ and _Delius_. A CRS referral is created in _Refer and Monitor an Intervention_, which makes an API call to 'Refer and Monitor and Delius' to create the referral NSI in _Delius_. If the process fails to create the Delius NSI the creation of the CRS referral in _Refer and Monitor an Intervention_ is aborted.

| Business Event        | API Endpoint                        |
|-----------------------|-------------------------------------|
| Create a CRS referral | PUT /probation-case/{crn}/referrals |

![Workflow Map](./img/randm-and-delius-workflow-create-crs-referral.svg)

#### Workflow: End CRS Referral

Ending a CRS referral in _Refer and Monitor an Intervention_ triggers activity in _Delius_ to update the linked NSI and add contacts. The information added to the probation case in _Delius_ inform the probation practitioner that the referral has been ended in _Refer and Monitor an Intervention_.

| Business Event     | HMPPS Domain Event Type     |
|--------------------|-----------------------------|
| End a CRS referral | intervention.referral.ended |

![Workflow Map](./img/randm-and-delius-workflow-end-crs-referral.svg)

#### Workflow: Action Plan Interactions

An action plan must be submitted by the CRS supplier in _Refer and Monitor an Intervention_ for approval by the probation practitioner managing the case. On both submission and approval in _Refer and Monitor an Intervention_ a domain event is raised and a _Delius_ contact is added to communicate the action plan status to probation practitioners.

| Business Event                     | HMPPS Domain Event Type            |
|------------------------------------|------------------------------------|
| Submit a CRS referral action plan  | intervention.action-plan.submitted |
| Approve a CRS referral action plan | intervention.action-plan.approved  |

![Workflow Map](./img/randm-and-delius-workflow-action-plan.svg)

### CRS Appointment Workflows

Creating CRS session appointments is a core function of _Refer and Monitor an Intervention_, however, the primary service for managing probation appointments is _Delius_. Appointments are therefore created in both systems and the _Delius_ appointment contact is linked to the _Refer and Monitor an Intervention_ session by adding the `APPOINTMENT.ID` to the `CONTACT.EXTERNAL_REFERENCE` table of the _Delius_ database. The status of the CRS appointments is updated in _Delius_ as the appointment sessions are delivered and recorded in _Refer and Monitor an Intervention_.

#### Context Map - Appointments

![Context Map](./img/randm-and-delius-appointment-context-map.svg)

### Workflow: Create CRS Appointment

Creating session appointments in _Refer and Monitor an Intervention_ involves a synchronous API call to create the corresponding _Delius_ appointment contact, with an appointment type, date, time and location. A successful API call will result in an appointment contact being created in _Delius_. Booking a session appointment relies on the date and time being available for the person on probation. Failure to create an appointment in _Delius_ is a runtime exception in _Refer and Monitor an Intervention_ and the appointment creation is aborted.

| Business Event                          | API Endpoint                                                  |
|-----------------------------------------|---------------------------------------------------------------|
| Create a initial assessment appointment | PUT /probation-case/{crn}/referrals/{referralId}/appointments |
| Create a delivery session appointment   | PUT /probation-case/{crn}/referrals/{referralId}/appointments |

### Workflow: CRS Appointment Reschedule

Rescheduling a CRS appointment in _Refer and Monitor an Intervention_ involves updating the linked appointment in _Delius_ with an outcome and creating a new appointment for the new date and time. This is necessary to ensure source appointment data for downstream processes in _Delius_ are maintained and the case recording is standardised. As this process involves multiple changes to _Delius_ database structures and it is possible for the process to fail if the date and time are not available it is achieved using a single synchronous API call to the same endpoint for appointment creation. The inclusion of the details of the appointment being replaced identifies the API calls as a reschedule rather than an initial creation.

| Business Event            | API Endpoint                                                  |
|---------------------------|---------------------------------------------------------------|
| Reschedule an appointment | PUT /probation-case/{crn}/referrals/{referralId}/appointments |

![Workflow Map](./img/randm-and-delius-workflow-appointment-reschedule.svg)

### Workflow: CRS Appointment Outcome and Feedback

Session outcomes and feedback for CRS appointments are recorded in _Refer and Monitor an Intervention_ by the CRS supplier. The session outcomes are recorded against the relevant _Delius_ appointment contact, with the appropriate alerts to probation practitioners when activity should be brought to their attention.

| Business Event                                          | HMPPS Domain Event Type                                                |
|---------------------------------------------------------|------------------------------------------------------------------------|
| Submit initial appointment session outcome and feedback | intervention.initial-assessment-appointment.session-feedback-submitted |
| Submit delivery session outcome and feedback            | intervention.session-appointment.session-feedback-submitted            |

![Workflow Map](./img/randm-and-delius-workflow-appointment-feedback.svg)

## Interfaces

### Message Formats

The service responds to various HMPPS Domain Event message via the
[Refer and Monitor and Delius Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/refer-and-monitor-and-delius-queue.tf).
The events are raised by the [HMPPS Refer and Monitor Service](https://github.com/ministryofjustice/hmpps-interventions-service) to communicate important events in the CRS referrals process.

Example [messages](./src/dev/resources/messages/) are in the development source tree.

Incoming messages are filtered on `eventType` by the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/114fda2e35b6a6c8ee08c3a54317154dcde2c336/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/refer-and-monitor-and-delius-queue.tf#L6-L12)

### API Access Control

API endpoints are secured by roles supplied by the _HMPPS Auth_ client used in the requests

| API Endpoint | Required Role       |
|--------------|---------------------|
| All          | ROLE\_CRS\_REFERRAL |

# Create and Vary a Licence and Delius

Supports integration between [Create and Vary a Licence](https://github.com/ministryofjustice/create-and-vary-a-licence) and [Delius](https://github.com/ministryofjustice/delius). This service listens to domain events raised when a licence is activated in _Create and Vary a Licence_, determines the licence conditions that will now apply to person on probation and creates these licence conditions in _Delius_ for the purposes of managing the licence supervision. Licence conditions are modelled differently across the two services and therefore this service uses a mapping defined in the _Delius_ database to determine the type of licence condition to create. The service also provides API endpoints providing probation case management information to _Create and Vary a Licence_.

## Business Need

Reducing re-keying of licence conditions across _Create and Vary a Licence_ and _Delius_ saving probation practitioners time and improving data quality

## Data Dependencies

_Create and Vary a Licence_ depends on _Delius_ data for background on the relevant **Person on Probation** and the specific **Probation Case** as well as which areas of the probation service have responsibility for the case. The API also enables _Create a Vary a Licence_ to add a specific **Role** to a _Delius_ **User**, which supports onboarding to the service without the need for manual work to update permissions.

### Context Map - Probation Data

![Context Map](./tech-docs/source/img/create-and-vary-a-licence-api-context-map.svg)

**Case Responsibility** is not a single concept in _Delius_ and therefore _Create and Vary a Licence_ uses a number of _Delius_ entities to determine which cases to show to it's users.

## Workflows

### Licence Condition Workflows

Licence conditions are managed in _Create and Vary a Licence_ and supervised by probation practitioners using _Delius_. Workflows across the two systems reduce the need for manual re-keying of the licence condition information.

#### Context Map - Licence Conditions

![Context Map](./tech-docs/source/img/create-and-vary-a-licence-activation-workflow-context-map.svg)

#### Workflow: Licence Activation

Activation of a licence on release from prison triggers creation of licence conditions in _Delius_. There are three types of licence condition **Standard Conditions**, **Additional Conditions** and **Bespoke Conditions**. The mapping process between _Create and Vary a Licence_ and _Delius_ determines how each licence condition is created in Delius. If the mapping between the _Create and Vary a Licence_ and _Delius_ conditions cannot be found the incoming domain event message will fail to process and the licence condition will not be automatically created.

| Business Event     | Message Event Type / Filter                 |
|--------------------|---------------------------------------------|
| Licence Activation | create-and-vary-a-licence.licence.activated |

![Workflow Map](./tech-docs/source/img/create-and-vary-a-licence-workflow-create-licence-condition.svg)

## Interfaces

### Message Formats

The service responds to HMPPS Domain Event messages via the
[Create and Vary a Licence and Delius Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/create-and-vary-a-licence-queue.tf).
The events are raised by the [Create and Vary a Licence Service](https://github.com/ministryofjustice/create-and-vary-a-licence-api)
to communicate activation of the licence.

Example [messages](./src/dev/resources/messages/) are in the development source tree.

Incoming messages are filtered on `eventType` by the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/create-and-vary-a-licence-queue.tf#L5-L9)

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint | Required Role                                    |
|--------------|--------------------------------------------------|
| All          | ROLE_PROBATION\_API_\_CVL_\_CASE_DETAIL |

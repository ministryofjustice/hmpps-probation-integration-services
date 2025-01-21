# Domain events and Delius
This domain event service enables enhancement of domain events from Delius. The domain events are retrieved from the database in batches, enhanced and published to SNS.

The supported domain event enhancers are:

| Message event type                | Description                                   |
| --------------------------------- | --------------------------------------------- |
| probation-case.engagement.created | Adds a detail URL to retrieve engagement data |

# Data dependencies
Domain event service depends on Delius for retrieving batches of domain events waiting to be processed. Each domain event will contain a unique ID, message body and message attributes. The message body will contain the event type.

## Context map - Domain Event Data

![](./img/domain-events-context-map.svg)

# Workflows
## Enhancing a domain event
Domain events are retrieved from the database in batches. Each domain event will be processed in order. The domain event will contain an event type within the message body. The event type is mapped to an enhancement if one exists, otherwise it is matched to a no enhancement catch-all which will just publish the original domain event. Once a domain event has been enhanced it will be published to SNS.

![](./img/domain-events-enhancement-workflow.svg)

# API Access Control
API endpoints are available to retrieve additional information related to a domain event. These URLs will be added to the domain event message by enhancers within the domain event service.

The API endpoints are secured by roles supplied by the HMPPS Auth client used in the requests.

| API endpoint                            | Required role                              |
| --------------------------------------- | ------------------------------------------ |
| probation-case.engagement.created/{crn} | PROBATION_API_\_DOMAIN_EVENTS_\_ENGAGEMENT |
# Justice Email and Delius

This integration service automates the processing of emails into Delius contacts.


## Data dependencies
This service depends on Delius to create new contacts.

### Context Map - Justice Email Data

![](../../doc/tech-docs/source/images/je-and-delius-context-map.svg)


## Workflows

| Business Event | Message Event Type / Filter |
| -------------- | --------------------------- |
| Email received | email.message.received      |

### Polling for unread emails
Create a domain event for each unread email. Once an email has been processed it will be marked as read.

![](../../doc/tech-docs/source/images/email-poll-workflow.svg)


###  Email Domain Event Received
Validate the email, convert to markdown and store as a contact in Delius.

![](../../doc/tech-docs/source/images/email-received-workflow.svg)

## Interfaces

### Message formats

The service responds to HMPPS Domain Event messages via the [Justice Email and Delius Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/justice-email-and-delius-queue.tf).

Example [messages](./src/dev/resources/messages/) are in the development source tree.

Incoming messages are filtered on `eventType` by the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/justice-email-and-delius-queue.tf)
# Manage POM Cases and Delius

## Business Need

Service that manages data exchange between Delius and the 'Manage POM Cases' service, which is part of the Offender Management in Custody (OMiC) model. Allocation of a Prison Offender Manager (POM) within 'Manage POM Cases' uses information from the probation case stored in Delius. All Prison Offender Manager allocations and handover dates must be reflected in the Delius supervision record.

## Context Map

![Context Map](./tech-docs/source/img/manage-pom-cases-and-delius-context-map.svg)

## Interfaces

The service manages three integration points between 'Manage POM Cases' and Delius.

1. Providing an API endpoint for probation case information held in Delius
2. Consuming a domain event raised by 'Manage POM Cases' when handover dates are changed
3. Consuming a domain event raised by 'Manage POM Cases' when POM allocation is updated

The domain events are raised by the [Manage POM Cases](https://github.com/ministryofjustice/offender-management-allocation-manager) service when significant changes are made to case allocation in custody.

### Domain Event Triggers

The service responds to an HMPPS Domain Event message via an
[SQS Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/manage-pom-cases-and-delius-queue.tf).

Example [messages](./src/dev/resources/messages/) are in the development source tree

| Business Event                                           | Message Class      | Message Event Type / Filter          |
|----------------------------------------------------------|--------------------|--------------------------------------|
| POM to COM handover of responsibility dates have changed | HMPPS Domain Event | offender-management.handover.changed |
| The POM allocated to the case has changed                | HMPPS Domain Event | offender-management.pom.allocated    |

## Workflows

### Prison Offender Manager Handover Dates

![Handover Dates Workflow](./tech-docs/source/img/manage-pom-cases-workflow-handover-dates.svg)

### Responsibility Change

![Responsibiliy Change Workflow](./tech-docs/source/img/manage-pom-cases-workflow-responsibility.svg)

## Delius Case Allocation

Delius records details of POM allocations, COM allocations and handover dates for a probation case. It also has the concept of a 'Responsible Officer' which is the member of staff currently responsible for the case. A case may have a number of POM and COM records in Delius, indicating the allocation history, however there can only be one Responsible Officer at any one time. This will be one of the currently active POM or COM. The integration service will update POM allocations and handover dates in Delius. It will not change the Responsible Officer in Delius unless the current responsible officer is the active POM and this is changed by the MPC allocation.

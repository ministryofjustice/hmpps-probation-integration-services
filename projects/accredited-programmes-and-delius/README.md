# Accredited Programmes and Delius

An integration service to enable
the [Accredited Programmes in Community](https://github.com/ministryofjustice/hmpps-accredited-programmes-manage-and-deliver-ui)
service to exchange data with Delius, supporting the replacement of the IAPS (Integrated Accredited Programme System)
and IM (Interventions Manager) systems.

## Business Need

The Accredited Programmes service needs to:

- Display probation case information
- Create and manage programme appointments in Delius
- Log status changes and important events to the Delius contact log

This service acts as the integration layer between the Accredited Programmes service and Delius, providing
synchronous APIs for probation case information and appointment management along with asynchronous consumption of domain
events for status changes.

### Context Map

![Context Map](../../doc/tech-docs/source/images/accredited-programmes-and-delius-context-map.svg)

## Workflows

### Status Changes

- Accredited Programmes updates the status for the person on a programme (e.g. "on programme", "programme completed", "
  withdrawn").
- The integration service adds a contact to Delius to inform the offender's practitioner of the status change.

| Business Event | HMPPS Domain Event                                        |
|----------------|-----------------------------------------------------------|
| Status change  | `accredited-programmes-community.referral.status-updated` |

### Appointments

- Programme attendance appointments and 3‑way meetings are created or updated in bulk by Accredited Programmes.
- Delius contacts are updated and linked to the Accredited Programmes session records via external reference.

| Business Event                                             | API Endpoint              |
|------------------------------------------------------------|---------------------------|
| Find appointments                                          | POST /appointments/search |
| Create appointments                                        | POST /appointments        |
| Update appointments (logging outcomes, rescheduling, etc.) | PUT /appointments         |
| Delete appointments                                        | DELETE /appointments      |

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client:

| API Endpoint  | Required Role                                          |
|---------------|--------------------------------------------------------|
| All endpoints | ROLE_PROBATION_API__ACCREDITED_PROGRAMMES__CASE_DETAIL |

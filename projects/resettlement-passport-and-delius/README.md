# Resettlement Passport and Delius

Service that provides API interfaces to Delius giving access probation case information needed by the resettlement process for prison leavers. We also provide APIs to add case note and appointment information to the Delius contact log.

## Business Need

Supporting the [Resettlement Passport API](https://github.com/ministryofjustice/hmpps-resettlement-passport-api) with information from Delius and enabling the service to add data to Delius as required.

## Data Dependencies

_Resettlement Passport_ depends on _Delius_ data for background on the relevant **Person on Probation** and the specific **Probation Case**. The _Resettlement Passport_ system creates _Delius_ **Contacts** to represent **Appointments** and **NOMIS Case Notes**.

## Context Map

![Context Map](./tech-docs/source/img/resettlement-passport-and-delius-context-map.svg)

## Workflows

### Create Appointment

Appointment contacts are created in Delius using a synchronous `POST` request as part of the _Resettlement Passport_ user workflow. No appointment data is stored in the _Resettlement Passport_ system

![Workflow Map](./tech-docs/source/img/resettlement-passport-and-delius-workflow-appointment.svg)

### Create Case Note

NOMIS case note contacts are created in Delius using a synchronous `POST` request as part of the _Resettlement Passport_ user workflow. No case note data is stored in the _Resettlement Passport_ system

![Workflow Map](./tech-docs/source/img/resettlement-passport-and-delius-workflow-casenote.svg)

## Interfaces

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint       | Required Role                                               |
|--------------------|-------------------------------------------------------------|
| /appointments      | PROBATION\_API\_\_RESETTLEMENT\_PASSPORT\_\_APPOINTMENT\_RW |
| /nomis-case-note/* | PROBATION\_API\_\_RESETTLEMENT\_PASSPORT\_\_APPOINTMENT\_RW |
| /probation-cases/* | PROBATION\_API\_\_RESETTLEMENT\_PASSPORT\_\_CASE\_DETAIL    |
| /duty-to-refer-nsi | PROBATION\_API\_\_RESETTLEMENT\_PASSPORT\_\_CASE\_DETAIL    |

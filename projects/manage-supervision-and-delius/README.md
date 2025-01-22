# Manage supervision and Delius

## Viewing Person on Probation data

This service provides an overview of the data held for a **Person on Probation**:

* Overview of the person, contact details and other personal data
* History of activity
* Sentence information
* Compliance information
* Risk flags
* Schedule information

## Admin tasks

The service also enables management of sentence appointments (creation and recording outcome) and user admin tasks.

The supported user admin tasks are:

* Viewing user and team case loads
* Viewing which staff are members of a team
* Checking access levels and office locations of a user


# Business need
The service provides a simplified view of the data relating to a **Person on Probation** which is held in the Delius system. The service is used by **Probation Practitioners** by providing a summary of the key information required for their day-to-day activities. The users can also use the service for managing sentencing appointments.
 

# Data dependencies
Manage People on Probation (MPoP) depends on Delius data for **Probation Cases** and details of a **Person on Probation**.

## Context Map - Manage People on Probation

![](./tech-docs/source/img/mpop-context-map.svg)

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint | Required Role                                            |
|--------------|----------------------------------------------------------|
| All          | ROLE_PROBATION\_API_\_MANAGE_A_SUPERVISION_\_CASE_DETAIL |
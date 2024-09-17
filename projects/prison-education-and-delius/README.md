# Prison Education and Delius

Prison Education is a DPS service that pulls together existing prisoner data related to activities and education, and also the creation of a work readiness profile for a prisoner. This profile is created and used by a Prison Education Lead to prepare an offender leaving prison to gain employment.

## Business Need

To provide the Prison Education system with background information from the probation systems, allowing better matching of candidates to potential work opportunities.

## Context Map

![Context Map](./tech-docs/source/img/prison-education-context-map.svg)

## Interfaces

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint | Required Role                                         |
|--------------|-------------------------------------------------------|
| All          | ROLE_PROBATION\_API_\_PRISON\_EDUCATION_\_CASE_DETAIL |

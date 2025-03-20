# Core person record and Delius

This service provides an API to get details of the Delius person record.

# Business need
Provides a background person on probation information to the client.

# Data dependencies
This integration service depends on Delius data for up-to-date data relating to a **Person on Probation**.


## Context Map - Probation Search data
![](./tech-docs/source/img/cpr-and-delius-context-map.svg)


# API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint                  | Required Role                             |
| ----------------------------- | ----------------------------------------- |
| /probation-cases/{identifier} | PROBATION_API_\_CORE_PERSON_\_CASE_DETAIL |
| /all-probation-cases          | PROBATION_API_\_CORE_PERSON_\_CASE_DETAIL |
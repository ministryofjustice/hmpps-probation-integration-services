# DPS and Delius

This service provides an API to retrieve documents and convictions for a Person on Probation.


## Business need
Provides a background person on probation information to the client.


## Data dependencies
This service retrieves up-to-date data on a **Person on Probation**, their **Documents** and **Convictions** from the Delius database.


### Context Map - Probation Search data
![](../../doc/tech-docs/source/images/dps-and-delius-context-map.svg)


## API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint              | Required Role                   |
| ------------------------- | ------------------------------- |
| /case/{nomisId}/documents | PROBATION_API_\_DPS_\_DOCUMENTS |
| /document/{id}            | PROBATION_API_\_DPS_\_DOCUMENTS |
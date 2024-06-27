# External API and Delius

API Service that provides access to probation case information recorded in Delius to the [HMPPS Integration API](https://ministryofjustice.github.io/hmpps-integration-api-docs/). The HMPPS Integration API supports supplying HMPPS data to external agencies such as the Home Office, Police and external suppliers of HMPPS digital services, using internal HMPPS APIs as data sources.

## Business Need

Supporting the HMPPS Integration API service with the correct and up-to-date information on the probation case as recorded in Delius.

## Context Map

![Context Map](./tech-docs/source/img/external-api-and-delius-context-map.svg)

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint | Required Role                                   |
|--------------|-------------------------------------------------|
| All          | ROLE\_PROBATION\_API_\_HMPPS\_API_\_CASE_DETAIL |
|              |                                                 |

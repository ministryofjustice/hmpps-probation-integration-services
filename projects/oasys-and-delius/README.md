# OASys and Delius

This integration service provides an API to retrieve details about a **Person on Probation** and **Case** from the Delius system:

* Case details
* Active registrations
* Latest release recall

## Business need
This integration service enables the OASys assessment system to retrieve up-to-date data from Delius about a Probation Case.


## Data dependencies
The service depends on Delius data for up to date information on a **Person on Probation**, details about the **Case**, **Registrations** and **Sentence** details.


### Context Map

![](../../doc/tech-docs/source/images/oasys-delius-context-map.svg)


## API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint                         | Required Role                       |
| ------------------------------------ | ----------------------------------- |
| /probation-cases/{crn}               | PROBATION_API_\_OASYS_\_CASE_DETAIL |
| /probation-cases/{crn}/registrations | PROBATION_API_\_OASYS_\_CASE_DETAIL |
| /probation-cases/{crn}/release       | PROBATION_API_\_OASYS_\_CASE_DETAIL |
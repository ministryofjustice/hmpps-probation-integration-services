# SOC and Delius

The SOC integration service provides APIs for the client to retrieve the required data.

APIs are available to enable read-only access to the following data:

* Convictions
* Court appearances
* Details related to a person on probation
* Probation area


## Business need
This integration service is used to gather the required Delius data via the APIs.


## Data dependencies
This service provides read-only access to data stored in Delius for a **Person on Probation**, their **Convictions** and **Court appearances**.


### Context Map

![](../../doc/tech-docs/source/images/soc-delius-context-map.svg)

## API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint            | Required Role                     |
|-------------------------|-----------------------------------|
| /convictions/{value}    | PROBATION_API_\_SOC_\_CASE_DETAIL |
| /court-appearances      | PROBATION_API_\_SOC_\_CASE_DETAIL |
| /detail/{value}         | PROBATION_API_\_SOC_\_CASE_DETAIL |
| /probation-areas        | PROBATION_API_\_SOC_\_CASE_DETAIL |
| /probation-area-history | PROBATION_API_\_SOC_\_CASE_DETAIL |
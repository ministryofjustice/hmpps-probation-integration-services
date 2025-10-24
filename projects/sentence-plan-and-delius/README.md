# Sentence plan and Delius

This integration service provides an API to get the details for the sentence plan for a person on probation.

## Business need
Provides background information for a person on probation to the client.


## Data dependencies
This depends on Delius for up-to-date information on the sentence plan.


### Context Map - Probation Search data
![](../../doc/tech-docs/source/images/sp-and-delius-context-map.svg)


## API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint                  | Required Role                               |
|-------------------------------|---------------------------------------------|
| /case-details/{crn}           | PROBATION_API_\_SENTENCE_PLAN_\_CASE_DETAIL |
| /user/{username}/access/{crn} | PROBATION_API_\_SENTENCE_PLAN_\_CASE_DETAIL |
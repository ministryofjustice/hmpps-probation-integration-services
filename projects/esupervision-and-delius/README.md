# esupervision-and-delius

This service provides an API to get contact details of the Delius person record
and to handle messages from the client to Delius for the purpose of "check ins".

## Business need

Provides contact details of a person on probation to the client.
Provides message handling for 'check in messages' from the client to Delius.

## Data dependencies

This integration service depends on Delius data for up-to-date data relating to a **Person on Probation**.

## API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint       | Required Role                            |
|--------------------|------------------------------------------|
| /case/{identifier} | PROBATION_API__ESUPERVISION__CASE_DETAIL |
| /cases             | PROBATION_API__ESUPERVISION__CASE_DETAIL |
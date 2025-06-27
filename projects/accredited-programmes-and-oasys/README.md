# Accredited programmes and OASys

This integration service provides an API to retrieve risk assessment information from OASys.

## Business need
Provides a summary of the risk of a person on probation to the client.


## Data dependencies
This depends on OASys for up-to-date information on the risk of a person on probation.


### Context Map - Probation Search data
![](../../doc/tech-docs/source/images/ap-and-oasys-context-map.svg)


## API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint   | Required Role                                      |
| -------------- | -------------------------------------------------- |
| /assessments/* | PROBATION_API_\_ACCREDITED_PROGRAMMES_\_ASSESSMENT |
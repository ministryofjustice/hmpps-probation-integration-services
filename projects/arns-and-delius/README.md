# ARNS and Delius

This integration service provides an API to get the limitations (exclusions and restrictions) for a user.

## Business need

To allow the Assess Risks and Needs (ARNS) service to prevent unauthorised access to a probation case.

## Data dependencies
This depends on Delius for up-to-date information on the restrictions and exclusions applied to a user.

### Context Map

![](../../doc/tech-docs/source/images/arns-and-delius-context-map.svg)


## API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint  | Required Role                      |
|---------------|------------------------------------|
| /users/access | PROBATION_API_\_ARNS_\_USER_ACCESS |
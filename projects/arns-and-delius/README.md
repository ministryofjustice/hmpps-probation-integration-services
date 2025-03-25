# ARNS and Delius

This integration service provides an API to get the limitations (exclusions and restrictions) for a user.

# Business need
Provides a background person on probation information to the client.

# Data dependencies
This depends on Delius for up-to-date information on the restrictions and exclusions applied to a user.

## Context Map - Probation Search data
![](./tech-docs/source/img/arns-and-delius-context-map.svg)


# API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint | Required Role                      |
| ------------ | ---------------------------------- |
| /users       | PROBATION_API_\_ARNS_\_USER_ACCESS |
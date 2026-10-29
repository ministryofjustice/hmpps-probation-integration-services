# EMDI and Delius

This service enables the [EMDI](https://github.com/ministryofjustice/hmpps-electronic-monitoring-data-insights-api/) service to retrieve user/team structure from NDelius so it can present the correct organisational context for users and support case workflows. It creates an event timeline from the locations of a tag attached to a person on probation on certain licence conditions.

## Business need

EMDI requires access to probation organisational reference data from Delius to support operational user journeys and ensure the correct teams, regions and PDUs are available to users.

This integration service provides read-only APIs for:

- retrieving regions and PDUs from Delius
- retrieving the teams associated with a user
- exposing the organisational structure required by EMDI to support user routing and service configuration

This reduces the need for EMDI to maintain duplicate reference data and keeps the service aligned with the source of truth in Delius.

## Interfaces

### API endpoints

The service exposes RESTful APIs for EMDI to retrieve the reference data it needs:

- `GET /regions`
    - Returns the list of regions configured in Delius
- `GET /regions/{regionCode}/pdu`
    - Returns the PDUs for a given region
- `GET /user/{username}/teams`
    - Returns the teams associated with a given user

These endpoints are secured and require the appropriate HMPPS Auth role.

### Message / event handling

This service does not currently consume or publish domain events. It is a reference-data integration service for read-only lookups from Delius.

## Context map

![Context Map](../../doc/tech-docs/source/images/emdi-and-delius-context-map.svg)

## Authorisation

API endpoints are protected by roles supplied by the HMPPS Auth client used in the requests.

| API Endpoint         | Required Role                                     |
|----------------------|---------------------------------------------------|
| `/regions/**`        | `ROLE_PROBATION_API__EMDI__REFERENCE_DATA`        |
| `/user/**`           | `ROLE_PROBATION_API__EMDI__REFERENCE_DATA`        |


# Jitbit and Delius

Read-only integration exposing basic probation case details to the Jitbit ticketing system.

## Business Need

- Allow the Jitbit ticketing system to look up probation case information to reduce staff needing to check multiple
  systems.
- Enforce Limited Access (LAO) restrictions so protected cases are not exposed in Jitbit.

## Data Dependencies

- Delius is currently the source of truth for case and person details (name, date of birth, addresses).
- HMPPS Auth provides user/role based access control used to secure the API.

## Context Map

![Context Map](../../doc/tech-docs/source/images/jitbit-and-delius-context-map.svg)

## API Access Control

All endpoints are secured by a HMPPS Auth role required by the Jitbit client.

| API Endpoint           | Required Role                        |
|------------------------|--------------------------------------|
| GET /case/{crn}        | PROBATION_API_\_JITBIT_\_CASE_DETAIL |
| GET /case/{crn}/access | PROBATION_API_\_JITBIT_\_CASE_DETAIL |

# Probation Access Control

Provides information on a user's access to a probation case.
In Delius, a user may be excluded from accessing a specific case or be granted access to a restricted case.

## Business Need

Provides a read-only API for other services to access LAO (Limited Access Offender) information from Delius.

## Context Map

![Context Map](../../doc/tech-docs/source/images/probation-access-control-context-map.svg)

## Authorisation

| API Endpoint               | Required Role                             |
|----------------------------|-------------------------------------------|
| /user/{username}/access/** | ROLE_PROBATION_API__ACCESS_CONTROLS__READ |

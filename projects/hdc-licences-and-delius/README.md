# HDC Licences and Delius

Supports integration between [HDC Licences](https://github.com/ministryofjustice/hmpps-hdc-api) and [Delius](https://github.com/ministryofjustice/delius). _HDC Licences_ is an HMPPS Digital service to manage applications for Home Detention Curfew, facilitating communication between the prison and probation staff managing the case. This involves assessing the details of the application (e.g. assess the proposed address for suitability) and submission for consideration in the custody release process.

## Business Need

Ensuring the user of the _HDC Licences_ has an up-to-date view of the probation case in _Delius_ when working on a licence application.

## Context Map

![Context Map](./tech-docs/source/img/hdc-licences-context-map.svg)

## Workflows

### Add HDC Role to Delius User

A _Delius_ identity may be used to authenticate and authorise access to the _HDC Licences_ system. A _Delius_ role determines whether an authenticated _Delius_ user can access _HDC Licences_ and these HDC-specific roles can be added to the _Delius_ user via an API call.

## Interfaces

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in the requests

| API Endpoint          | Required Role                           |
|-----------------------|-----------------------------------------|
| PUT /users/{username} | PROBATION\_API_\_HDC_\_USER\_ROLES_\_RW |
| GET /staff/*          | PROBATION\_API_\_HDC_\_STAFF            |
| GET /providers/*      | PROBATION\_API_\_HDC_\_STAFF            |
| GET /case/*           | PROBATION\_API_\_HDC_\_STAFF            |

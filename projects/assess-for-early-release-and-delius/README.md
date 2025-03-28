# Assess for Early Release and Delius

This service provides an API to retrieve details about a person on probation, their manager and the caseload.

# Business need
Provides a background on person on probation information to the client.

# Data dependencies
This service retrieves up-to-date data on a person on probation, their manager and the caseload from Delius.

## Context Map - Probation Search data
![](./tech-docs/source/img/afer-and-delius-context-map.svg)


# API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint      | Required Role                                          |
| ----------------- | ------------------------------------------------------ |
| /probation-case/* | PROBATION_API_\_ASSESS_FOR_EARLY_RELEASE_\_CASE_DETAIL |
| /staff/*          | PROBATION_API_\_ASSESS_FOR_EARLY_RELEASE_\_CASE_DETAIL |
| /team/*           | PROBATION_API_\_ASSESS_FOR_EARLY_RELEASE_\_CASE_DETAIL |
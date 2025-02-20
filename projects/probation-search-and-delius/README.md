# Probation Search and Delius

Service to enable auditing and provide access to Delius data from
the [Probation Search API](https://github.com/ministryofjustice/probation-offender-search).

This service provides two API endpoints:

* Get for contacts for a **Person on Probation**
* Store an audit log for a **Contact** search


# Business need
This service can be used by an authenticated user to retrieve the contacts for a **Person on Probation**. The service is also used to maintain an audit trail of the contact searches.


# Data dependencies
This service depends on Delius data to enable a search on **Contact** data for a **Person on Probation**. The service will also handle writes into the audit log database for searches performed by a user.


## Context Map - Probation Search data
![](./tech-docs/source/img/probation-search-context-map.svg)


# API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint          | Required Role                                  |
| --------------------- | ---------------------------------------------- |
| /case/{crn}/contacts  | PROBATION_API_\_PROBATION_SEARCH_\_CASE_DETAIL |
| /contact-search       | PROBATION_API_\_PROBATION_SEARCH_\_AUDIT_RW    |
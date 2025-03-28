# Subject Access Requests and Delius

This integration service provides an API to get person details from Delius for SAR.


# Data dependencies
This depends on Delius for up-to-date information.


## Context Map - Probation Search data
![](./tech-docs/source/img/sar-and-delius-context-map.svg)


# API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint          | Required Role                                   |
| --------------------- | ----------------------------------------------- |
| /probation-case/{crn} | PROBATION_API_\_SUBJECT_ACCESS_REQUEST_\_DETAIL |
| /user                 | PROBATION_API_\_SUBJECT_ACCESS_REQUEST_\_DETAIL |
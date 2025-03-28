# IMS and Delius

This integration service provides an API to manage user roles.


# Data dependencies
This enables modification of the Delius data for the user role.


# API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint          | Required Role                                |
| --------------------- | -------------------------------------------- |
| /user/{username}/role | PROBATION_API_\_PATHFINDER_\_USER_ROLES_\_RW |
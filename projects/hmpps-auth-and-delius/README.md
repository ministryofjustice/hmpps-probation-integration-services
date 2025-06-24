# HMPPS Auth and Delius

This service acts as an identity provider for the HMPPS Auth service to enable Delius users to authenticate.


## Business need

This service enables users to sign into their account using HMPPS Auth.


## Data dependencies

### Context Map - Authentication and user data

![](../../doc/tech-docs/source/images/auth-context-map.svg)

## API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in the requests.

| API endpoint              | Required Role                             |
| ------------------------- | ----------------------------------------- |
| /authenticate             | PROBATION_API_\_HMPPS_AUTH_\_AUTHENTICATE |
| /user                     | PROBATION_API_\_HMPPS_AUTH_\_USER_DETAILS |
| /user/{username}          | PROBATION_API_\_HMPPS_AUTH_\_USER_DETAILS |
| /user/details/{userId}    | PROBATION_API_\_HMPPS_AUTH_\_USER_DETAILS |
| /user/{username}/password | PROBATION_API_\_HMPPS_AUTH_\_PASSWORD_RW  |
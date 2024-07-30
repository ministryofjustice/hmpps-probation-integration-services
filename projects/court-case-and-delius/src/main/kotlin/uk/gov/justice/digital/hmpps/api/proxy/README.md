# Community API Proxy

The Community API controller has the same endpoints as community API.
This performs a JSON comparison between the new versions of the API and the commumity API endpoints and sends to App INsights.

##  Comparison Endpoint
The Community API controller has a paged POST secure/compareAll endpoint.
This takes a bearer token that must contain both the ROLE_COMMUNITY and ROLE_PROBATION_API__COURT_CASE__CASE_DETAIL roles.

This takes the following payload, where all endpoints can be specified.
The crns section is optional. If no crns are specified, then all crns (controlled by the page size and page number) are retrieved from the database.
The params marked with "?" will be automatically populated from the database.

    {
        "pageNumber": 1,
        "pageSize": 1,
        "crns": [ "C123456", "P123456"],
        "uriConfig": {
            "OFFENDER_DETAIL": {},
            "OFFENDER_SUMMARY": {},
            "OFFENDER_MANAGERS": {
                "includeProbationAreaTeams": false
            },
            "CONVICTIONS": {
                "activeOnly": false
            },
            "CONVICTION_BY_ID": {
                "convictionId": "?"
            },
            "CONVICTION_REQUIREMENTS": {
                "convictionId": "?",
                "activeOnly": true,
                "excludeSoftDeleted": true
            },
            "CONVICTION_BY_ID_NSIS": {
                "convictionId": "?",
                "nsiCodes": "?"
            },
            "CONVICTION_BY_ID_PSS": {
                "convictionId": "?"
            }
        }
    }
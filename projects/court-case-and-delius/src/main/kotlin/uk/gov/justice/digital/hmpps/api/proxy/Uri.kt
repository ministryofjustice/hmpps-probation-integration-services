package uk.gov.justice.digital.hmpps.api.proxy

enum class Uri(val comApiUrl: String, val ccdInstance: String, val ccdFunction: String) {
    OFFENDER_DETAIL("/secure/offenders/crn/{crn}/all", "probationRecordResource", "getOffenderDetail"),
    OFFENDER_SUMMARY("/secure/offenders/crn/{crn}", "probationRecordResource", "getOffenderDetailSummary"),
    DUMMY("/dummy", "dummyResource", "getDummy"),
}
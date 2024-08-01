package uk.gov.justice.digital.hmpps.api.proxy

enum class Uri(
    val comApiUrl: String,
    val ccdInstance: String,
    val ccdFunction: String,
    val urlParams: List<String>
) {
    OFFENDER_DETAIL("/secure/offenders/crn/{crn}/all", "probationRecordResource", "getOffenderDetail", listOf("crn")),
    OFFENDER_SUMMARY(
        "/secure/offenders/crn/{crn}",
        "probationRecordResource",
        "getOffenderDetailSummary",
        listOf("crn")
    ),
    OFFENDER_MANAGERS(
        "/secure/offenders/crn/{crn}/allOffenderManagers?includeProbationAreaTeams={includeProbationAreaTeams}",
        "probationRecordResource",
        "getAllOffenderManagers",
        listOf("crn", "includeProbationAreaTeams")
    ),
    CONVICTIONS(
        "/secure/offenders/crn/{crn}/convictions?activeOnly={activeOnly}",
        "convictionResource",
        "getConvictionsForOffenderByCrn",
        listOf("crn", "activeOnly")
    ),
    CONVICTION_BY_ID(
        "/secure/offenders/crn/{crn}/convictions/{convictionId}",
        "convictionResource",
        "getConvictionForOffenderByCrnAndConvictionId",
        listOf("crn", "convictionId")
    ),
    CONVICTION_REQUIREMENTS(
        "/secure/offenders/crn/{crn}/convictions/{convictionId}/requirements?activeOnly={activeOnly}&excludeSoftDeleted={excludeSoftDeleted}",
        "convictionResource",
        "getRequirementsForConviction",
        listOf("crn", "convictionId", "activeOnly", "excludeSoftDeleted"),
    ),
    CONVICTION_BY_ID_NSIS(
        "/secure/offenders/crn/{crn}/convictions/{convictionId}/nsis?nsiCodes={nsiCodes}",
        "convictionResource",
        "getNsisByCrnAndConvictionId",
        listOf("crn", "convictionId", "nsiCodes"),
    ),
    CONVICTION_BY_ID_PSS(
        "/secure/offenders/crn/{crn}/convictions/{convictionId}/pssRequirements",
        "convictionResource",
        "getPssRequirementsByConvictionId",
        listOf("crn", "convictionId"),
    ),

    DUMMY("/dummy", "dummyResource", "getDummy", listOf("crn")),
}
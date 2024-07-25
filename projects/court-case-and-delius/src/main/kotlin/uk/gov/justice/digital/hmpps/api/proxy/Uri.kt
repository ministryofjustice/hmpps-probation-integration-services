package uk.gov.justice.digital.hmpps.api.proxy

enum class Uri(
    val comApiUrl: String,
    val ccdInstance: String,
    val ccdFunction: String,
    val urlParams: List<String>
) {
    OFFENDER_DETAIL("/secure/offenders/crn/{crn}/all", "offenderService", "getOffenderDetail", listOf("crn")),
    OFFENDER_SUMMARY("/secure/offenders/crn/{crn}", "offenderService", "getOffenderDetailSummary", listOf("crn")),
    OFFENDER_MANAGERS(
        "/secure/offenders/crn/{crn}/allOffenderManagers?includeProbationAreaTeams={includeProbationAreaTeams}",
        "offenderManagerService",
        "getAllOffenderManagersForCrn",
        listOf("crn", "includeProbationAreaTeams")
    ),
    CONVICTIONS(
        "/secure/offenders/crn/{crn}/convictions?activeOnly={activeOnly}", "convictionResource", "getConvictionsForOffenderByCrn",
        listOf("crn", "activeOnly")
    ),
    CONVICTION_REQUIREMENTS(
        "/secure/offenders/crn/{crn}/{convictionId}/requirements?activeOnly={activeOnly}&excludeSoftDeleted={excludeSoftDeleted}",
        "convictionResource",
        "getRequirementsForConviction",
        listOf("crn", "convictionId", "activeOnly", "excludeSoftDeleted"),
    ),
    DUMMY("/dummy", "dummyResource", "getDummy", listOf("crn")),
}
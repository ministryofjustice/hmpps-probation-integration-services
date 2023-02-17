package uk.gov.justice.digital.hmpps.api.model

data class OfficerView(
    val code: String,
    val name: Name,
    val grade: String?,
    val email: String?,
    val casesDueToEndInNext4Weeks: Long,
    val releasesWithinNext4Weeks: Long,
    val paroleReportsToCompleteInNext4Weeks: Long
)

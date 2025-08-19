package uk.gov.justice.digital.hmpps.model

data class UserCaseloadIndicator(
    val inCaseload: Boolean,
    val userExcluded: Boolean,
    val userRestricted: Boolean,
    val exclusionMessage: String?,
    val restrictionMessage: String?
) {
    val canAccess: Boolean = inCaseload && !userRestricted && !userExcluded
}
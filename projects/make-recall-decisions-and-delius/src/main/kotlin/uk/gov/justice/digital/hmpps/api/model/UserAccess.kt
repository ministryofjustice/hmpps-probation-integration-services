package uk.gov.justice.digital.hmpps.api.model

data class UserAccess(
    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null,
) {
    val userExcluded: Boolean = exclusionMessage != null
    val userRestricted: Boolean = restrictionMessage != null
}

package uk.gov.justice.digital.hmpps.api.model

data class UserAccess(
    val userExcluded: Boolean,
    val userRestricted: Boolean,
    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null
) {
    companion object {
        val NO_ACCESS_LIMITATIONS = UserAccess(userExcluded = false, userRestricted = false)
    }
}

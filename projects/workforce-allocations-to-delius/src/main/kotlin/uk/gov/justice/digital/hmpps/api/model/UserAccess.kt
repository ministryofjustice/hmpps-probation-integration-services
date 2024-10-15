package uk.gov.justice.digital.hmpps.api.model

data class UserAccess(val access: List<CaseAccess>)

data class CaseAccess(
    val crn: String,
    val userExcluded: Boolean,
    val userRestricted: Boolean,
    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null
)

data class CaseAccessList(
    val crn: String,
    val excludedFrom: List<User>,
    val restrictedTo: List<User>,
    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null,
)

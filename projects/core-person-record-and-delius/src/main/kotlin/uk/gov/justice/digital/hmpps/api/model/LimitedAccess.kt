package uk.gov.justice.digital.hmpps.api.model

data class LimitedAccess(
    val message: String?,
    val users: List<LimitedAccessUser>
)

data class LimitedAccessUser(
    val username: String
)
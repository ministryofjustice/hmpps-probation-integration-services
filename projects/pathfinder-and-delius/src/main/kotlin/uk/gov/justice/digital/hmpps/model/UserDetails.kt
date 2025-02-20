package uk.gov.justice.digital.hmpps.model

data class UserDetails(
    val username: String,
    val forename: String,
    val surname: String,
    val email: String?,
)
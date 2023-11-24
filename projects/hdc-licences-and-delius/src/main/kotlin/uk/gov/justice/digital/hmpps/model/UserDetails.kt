package uk.gov.justice.digital.hmpps.model

data class UserDetails(
    val username: String,
    val enabled: Boolean,
    val roles: List<String>
)

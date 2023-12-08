package uk.gov.justice.digital.hmpps.api.model

data class UserDetail(
    val username: String,
    val name: Name,
    val email: String?,
)

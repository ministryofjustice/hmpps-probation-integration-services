package uk.gov.justice.digital.hmpps.api.model.user

data class UserSearchFilter(
    val nameOrCrn: String? = null,
    val sentence: String? = null,
    val nextContact: String? = null
)

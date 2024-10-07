package uk.gov.justice.digital.hmpps.api.model.user

data class UserSearchFilter(
    val nameOrCrn: String? = null,
    val sentenceCode: String? = null,
    val nextContactCode: String? = null
)

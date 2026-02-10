package uk.gov.justice.digital.hmpps.api.model.contact

data class ContactTypesResponse(
    val contactTypes: List<ContactTypeResponse>
)

data class ContactTypeResponse(
    val code: String,
    val description: String,
    val isPersonLevelContact: Boolean
)
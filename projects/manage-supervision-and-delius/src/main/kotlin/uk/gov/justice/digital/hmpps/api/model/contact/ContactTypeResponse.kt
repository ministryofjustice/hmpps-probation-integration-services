package uk.gov.justice.digital.hmpps.api.model.contact

data class ContactTypesResponse(
    val contactTypes: List<ContactTypeResponse>
)

data class ContactTypeResponse(
    val code: String,
    val description: String,
    val isPersonLevelContact: Boolean
)

data class EnforcementActionResponse(
    val code: String,
    val description: String,
    val defaultResponsePeriodDays: Long?
)

data class ContactOutcomeResponse(
    val code: String,
    val description: String,
    val enforcementActions: List<EnforcementActionResponse>
)

data class ContactOutcomes(
    val outcomes: List<ContactOutcomeResponse>
)
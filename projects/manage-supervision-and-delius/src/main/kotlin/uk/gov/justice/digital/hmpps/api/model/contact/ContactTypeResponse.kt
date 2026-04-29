package uk.gov.justice.digital.hmpps.api.model.contact

import uk.gov.justice.digital.hmpps.api.model.CodeAndDescription

data class ContactTypesResponse(
    val contactTypes: List<ContactTypeResponse>
)

data class ContactTypeResponse(
    val code: String,
    val description: String,
    val isPersonLevelContact: Boolean
)

data class ContactOutcomes(
    val outcomes: List<CodeAndDescription>
)
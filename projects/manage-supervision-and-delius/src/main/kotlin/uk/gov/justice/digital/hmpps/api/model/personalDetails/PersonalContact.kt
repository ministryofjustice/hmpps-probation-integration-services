package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name

data class PersonalContact(
    val name: Name,
    val relationship: String?,
    val relationshipType: String,
    val address: ContactAddress?,
    val notes: String?
)
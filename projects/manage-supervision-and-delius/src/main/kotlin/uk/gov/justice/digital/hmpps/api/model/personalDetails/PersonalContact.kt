package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class PersonalContact(
    val personSummary: PersonSummary,
    val contactId: Long,
    val name: Name,
    val relationship: String?,
    val relationshipType: String,
    val address: ContactAddress?,
    val notes: String?
)
package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.Name

data class ProfessionalContact(
    val name: Name,
    val currentContacts: List<Contact> = emptyList(),
    val previousContacts: List<Contact> = emptyList()
)

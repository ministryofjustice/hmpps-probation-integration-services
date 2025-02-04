package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import java.time.LocalDate

data class PersonalContact(
    val personSummary: PersonSummary,
    val contactId: Long,
    val name: Name,
    val relationship: String?,
    val relationshipType: String,
    val address: ContactAddress?,
    val notes: List<NoteDetail>? = null,
    val note: NoteDetail? = null,
    val phone: String?,
    val email: String?,
    val startDate: LocalDate?,
    val lastUpdated: LocalDate,
    val lastUpdatedBy: Name
)
package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import java.time.LocalDate

data class DisabilityOverview(
    val personSummary: PersonSummary,
    val disabilities: List<Disability>

)

data class Disability(
    val disabilityId: Int,
    val description: String,
    val notes: List<NoteDetail> = listOf(),
    val startDate: LocalDate,
    val lastUpdated: LocalDate,
    val lastUpdatedBy: Name
)
package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import java.time.LocalDate

data class CircumstanceOverview(
    val personSummary: PersonSummary,
    val circumstances: List<Circumstance>,
    val previousCircumstances: List<Circumstance>
)

data class CircumstanceOverviewSummary(
    val personSummary: PersonSummary,
    val circumstance: Circumstance?
)

data class Circumstance(
    val id: Long,
    val type: String,
    val subType: String,
    val circumstanceNotes: List<NoteDetail>? = null,
    val circumstanceNote: NoteDetail? = null,
    val verified: Boolean?,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val lastUpdated: LocalDate,
    val lastUpdatedBy: Name
)
package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import java.time.LocalDate

data class ProvisionOverview(
    val personSummary: PersonSummary,
    val provisions: List<Provision>
)

data class ProvisionOverviewSummary(
    val personSummary: PersonSummary,
    val provision: Provision?
)

data class Provision(
    val id: Long,
    val description: String,
    val provisionNotes: List<NoteDetail>? = null,
    val provisionNote: NoteDetail? = null,
    val startDate: LocalDate,
    val lastUpdated: LocalDate,
    val lastUpdatedBy: Name
)
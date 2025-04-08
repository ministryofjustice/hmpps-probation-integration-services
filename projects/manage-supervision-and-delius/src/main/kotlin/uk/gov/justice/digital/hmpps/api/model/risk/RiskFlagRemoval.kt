package uk.gov.justice.digital.hmpps.api.model.risk

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import java.time.LocalDate

data class RiskFlagRemoval(
    val riskRemovalNotes: List<NoteDetail>? = null,
    val riskRemovalNote: NoteDetail? = null,
    val removalDate: LocalDate,
    val removedBy: Name
)
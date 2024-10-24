package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import java.time.LocalDate

data class LicenceCondition(
    val id: Long,
    val mainDescription: String,
    val subTypeDescription: String? = null,
    val imposedReleasedDate: LocalDate,
    val actualStartDate: LocalDate? = null,
    val licenceConditionNotes: List<LicenceConditionNote>? = null,
    val licenceConditionNote: LicenceConditionNote? = null
)

data class LicenceConditionNote(
    val id: Int,
    val createdBy: String? = null,
    val createdByDate: LocalDate? = null,
    val note: String,
    val hasNoteBeenTruncated: Boolean? = null
)

data class LicenceConditionNoteDetail(
    val personSummary: PersonSummary,
    val licenceCondition: LicenceCondition? = null
)
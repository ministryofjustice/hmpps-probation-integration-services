package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class LicenceCondition(
    val id: Long,
    val mainDescription: String,
    val subTypeDescription: String? = null,
    val imposedReleasedDate: LocalDate,
    val actualStartDate: LocalDate? = null,
    val notes: List<LicenceConditionNote>? = null
)

data class LicenceConditionNote(
    val id: Int,
    val createdBy: String? = null,
    val createdByDate: LocalDate? = null,
    val note: String,
    val hasNotesBeenTruncated: Boolean? = null
)

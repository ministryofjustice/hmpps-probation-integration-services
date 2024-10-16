package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class LicenceCondition(
    val mainDescription: String,
    val subTypeDescription: String? = null,
    val imposedReleasedDate: LocalDate,
    val actualStartDate: LocalDate? = null,
    val notes: List<LicenceConditionNote> = listOf()
)

data class LicenceConditionNote(
    val createdBy: String? = null,
    val createdByDate: LocalDate? = null,
    val note: String,
    val hasNotesBeenTruncated: Boolean? = null
)

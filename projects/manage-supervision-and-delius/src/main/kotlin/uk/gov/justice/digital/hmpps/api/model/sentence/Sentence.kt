package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.appointment.MinimalNsi
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import java.time.LocalDate

data class Sentence(
    val offenceDetails: OffenceDetails,
    val conviction: Conviction? = null,
    val order: Order? = null,
    val requirements: List<Requirement>?,
    val courtDocuments: List<CourtDocument> = listOf(),
    val unpaidWorkProgress: String? = null,
    val licenceConditions: List<LicenceCondition>?
)

data class MinimalSentence(
    val id: Long,
    val order: MinimalOrder? = null,
    val nsis: List<MinimalNsi> = emptyList(),
    val licenceConditions: List<MinimalLicenceCondition> = listOf(),
    val requirements: List<MinimalRequirement> = listOf()
)

data class MinimalOrder(
    val description: String,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)

data class NoteDetail(
    val id: Int,
    val createdBy: String? = null,
    val createdByDate: LocalDate? = null,
    val note: String,
    val hasNoteBeenTruncated: Boolean? = null
)
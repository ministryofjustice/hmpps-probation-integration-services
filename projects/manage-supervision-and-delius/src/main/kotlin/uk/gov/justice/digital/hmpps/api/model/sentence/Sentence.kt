package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.overview.Order
import java.time.LocalDate

data class Sentence(
    val offenceDetails: OffenceDetails,
    val conviction: Conviction? = null,
    val order: Order? = null,
    val requirements: List<Requirement> = listOf(),
    val courtDocuments: List<CourtDocument> = listOf(),
    val unpaidWorkProgress: String? = null,
    val licenceConditions: List<LicenceCondition> = listOf()
)

data class MinimalSentence(
    val id: Long,
    val order: MinimalOrder? = null,
    val licenceConditions: List<MinimalLicenceCondition> = listOf(),
    val requirements: List<MinimalRequirement> = listOf()
)

data class MinimalOrder(
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
)

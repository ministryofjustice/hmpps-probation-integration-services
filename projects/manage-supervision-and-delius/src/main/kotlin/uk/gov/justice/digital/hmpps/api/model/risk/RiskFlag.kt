package uk.gov.justice.digital.hmpps.api.model.risk

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class RiskFlag(
    val id: Long,
    val description: String,
    val levelCode: String?,
    val levelDescription: String?,
    val notes: String?,
    val nextReviewDate: LocalDate?,
    val mostRecentReviewDate: LocalDate?,
    val createdDate: LocalDate,
    val createdBy: Name,
    val removed: Boolean,
    val removalHistory: List<RiskFlagRemoval>
)
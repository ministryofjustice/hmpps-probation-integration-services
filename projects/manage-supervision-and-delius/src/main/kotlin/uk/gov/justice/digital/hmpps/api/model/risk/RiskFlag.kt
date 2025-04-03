package uk.gov.justice.digital.hmpps.api.model.risk

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import java.time.LocalDate

data class RiskFlag(
    val id: Long,
    val description: String,
    val level: RiskLevel,
    val levelCode: String?,
    val levelDescription: String?,
    val riskNotes: List<NoteDetail>? = null,
    val riskNote: NoteDetail? = null,
    val nextReviewDate: LocalDate?,
    val mostRecentReviewDate: LocalDate?,
    val createdDate: LocalDate,
    val createdBy: Name,
    val removed: Boolean,
    val removalHistory: List<RiskFlagRemoval>
)

enum class RiskLevel(val severity: Int, val value: String) {
    HIGH(4, "RED"), MEDIUM(3, "AMBER"), LOW(2, "GREEN"), INFORMATION_ONLY(1, "WHITE");

    companion object {
        fun fromString(enumValue: String): RiskLevel {
            return entries.first { it.value == enumValue.uppercase() }
        }
    }
}

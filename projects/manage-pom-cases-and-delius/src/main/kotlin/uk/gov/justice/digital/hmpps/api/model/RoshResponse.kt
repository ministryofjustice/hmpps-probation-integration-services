package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class RoshResponse(
    val startDate: LocalDate,
    val level: RoshLevel
)

enum class RoshLevel(val code: String) {
    LOW("RLRH"),
    MEDIUM("RMRH"),
    HIGH("RHRH"),
    VERY_HIGH("RVRH");

    companion object {
        fun fromCode(code: String): RoshLevel = entries.first { it.code == code }
    }
}
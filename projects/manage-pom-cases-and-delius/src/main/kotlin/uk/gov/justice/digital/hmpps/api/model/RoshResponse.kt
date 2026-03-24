package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class RoshResponse(
    val startDate: LocalDate,
    val level: String
)

enum class RoshCode(val code: String) {
    LOW("RVH"),
    MEDIUM("RMRH"),
    HIGH("RHRH"),
    VERY_HIGH("RVH");

    companion object {
        fun fromCode(code: String): RoshCode = entries.first { it.code == code }
    }
}
package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class UnpaidWorkDetails(
    val unpaidWorkDetails: List<UnpaidWorkMinutes>
)

data class UnpaidWorkMinutes(
    val eventNumber: Long,
    val sentenceDate: LocalDate,
    val requiredMinutes: Long,
    val adjustments: Long,
    val completedMinutes: Long,
    val completedEteMinutes: Long,
)

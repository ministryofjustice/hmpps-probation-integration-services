package uk.gov.justice.digital.hmpps.model

data class UnpaidWorkDetails(
    val unpaidWorkDetails: List<UnpaidWorkMinutes>
)

data class UnpaidWorkMinutes(
    val eventNumber: Long,
    val requiredMinutes: Long,
    val adjustments: Long,
    val completedMinutes: Long,
    val completedEteMinutes: Long,
)

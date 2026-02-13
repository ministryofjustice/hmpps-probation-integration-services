package uk.gov.justice.digital.hmpps.model

data class RequirementProgress(
    val requiredMinutes: Long,
    val completedMinutes: Long,
    val adjustments: Long,
)
package uk.gov.justice.digital.hmpps.api.model

data class ProbationStatusDetail(
    val status: ManagedStatus
)

enum class ManagedStatus {
    CURRENTLY_MANAGED,
    PREVIOUSLY_MANAGED,
    NEW_TO_PROBATION,
    UNKNOWN
}
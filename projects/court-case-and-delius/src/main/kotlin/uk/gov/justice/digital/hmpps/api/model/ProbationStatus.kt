package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ProbationStatusDetail(
    val status: ManagedStatus,
    val terminationDate: LocalDate?,
    val inBreach: Boolean,
    val preSentenceActivity: Boolean,
    val awaitingPsr: Boolean
)

enum class ManagedStatus {
    CURRENTLY_MANAGED,
    PREVIOUSLY_MANAGED,
    NEW_TO_PROBATION,
    UNKNOWN
}

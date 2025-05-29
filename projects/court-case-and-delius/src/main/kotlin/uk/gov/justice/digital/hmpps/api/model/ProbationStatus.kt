package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ProbationStatusDetail(
    val status: ProbationStatus,
    val terminationDate: LocalDate? = null,
    val inBreach: Boolean = false,
    val preSentenceActivity: Boolean = false,
    val awaitingPsr: Boolean = false
) {
    companion object {
        val NO_RECORD = ProbationStatusDetail(ProbationStatus.NO_RECORD)
    }
}

enum class ProbationStatus {
    NO_RECORD,
    NOT_SENTENCED,
    PREVIOUSLY_KNOWN,
    CURRENT
}

data class ProbationCase(val offenderId: Long, val otherIds: Ids, val probationStatus: ProbationStatusDetail)
data class Ids(
    val crn: String,
    val nomsNumber: String? = null,
    val pncNumber: String? = null,
    val croNumber: String? = null
)
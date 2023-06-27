package uk.gov.justice.digital.hmpps.service.custody

enum class CustodialStatusCode(val code: String) {
    SENTENCED_IN_CUSTODY("A"),
    RELEASED_ON_LICENCE("B"),
    RECALLED("C"),
    IN_CUSTODY("D"),
    IN_CUSTODY_IRC("I"),
    POST_SENTENCE_SUPERVISION("P"),
    CUSTODY_ROTL("R"),
    TERMINATED("T"),
    AUTO_TERMINATED("AT");

    companion object {
        fun withCode(code: String) =
            values().firstOrNull { it.code == code } ?: throw IllegalArgumentException("Unknown custodial status")
    }
}

val TERMINATED_STATUSES = listOf(CustodialStatusCode.TERMINATED, CustodialStatusCode.AUTO_TERMINATED)
val NO_CHANGE_STATUSES = listOf(
    CustodialStatusCode.SENTENCED_IN_CUSTODY,
    CustodialStatusCode.IN_CUSTODY,
    CustodialStatusCode.IN_CUSTODY_IRC
)
val NO_RECALL_STATUSES = listOf(
    CustodialStatusCode.RECALLED
) + NO_CHANGE_STATUSES + TERMINATED_STATUSES

val RELEASABLE_STATUSES = listOf(
    CustodialStatusCode.IN_CUSTODY,
    CustodialStatusCode.SENTENCED_IN_CUSTODY,
    CustodialStatusCode.RECALLED
)

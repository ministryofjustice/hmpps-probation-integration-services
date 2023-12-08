package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown

enum class CustodialStatusCode(val code: String) {
    SENTENCED_IN_CUSTODY("A"),
    RELEASED_ON_LICENCE("B"),
    RECALLED("C"),
    IN_CUSTODY("D"),
    IN_CUSTODY_IRC("I"),
    POST_SENTENCE_SUPERVISION("P"),
    CUSTODY_ROTL("R"),
    TERMINATED("T"),
    AUTO_TERMINATED("AT"),
    ;

    companion object {
        fun withCode(code: String) =
            entries.firstOrNull { it.code == code } ?: throw IllegalArgumentException("Unknown custodial status")
    }
}

val TERMINATED_STATUSES = listOf(CustodialStatusCode.TERMINATED, CustodialStatusCode.AUTO_TERMINATED)
val NO_CHANGE_STATUSES =
    listOf(
        CustodialStatusCode.IN_CUSTODY,
        CustodialStatusCode.IN_CUSTODY_IRC,
        CustodialStatusCode.POST_SENTENCE_SUPERVISION,
    ) + TERMINATED_STATUSES
val CAN_RECALL_STATUSES =
    listOf(
        CustodialStatusCode.RELEASED_ON_LICENCE,
        CustodialStatusCode.CUSTODY_ROTL,
    )

val CAN_RELEASE_STATUSES =
    listOf(
        CustodialStatusCode.IN_CUSTODY,
        CustodialStatusCode.SENTENCED_IN_CUSTODY,
        CustodialStatusCode.RECALLED,
    )

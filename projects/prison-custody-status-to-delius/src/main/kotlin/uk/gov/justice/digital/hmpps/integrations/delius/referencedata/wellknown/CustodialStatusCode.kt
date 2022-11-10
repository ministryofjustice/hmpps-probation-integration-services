package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown

enum class CustodialStatusCode(val code: String) {
    SENTENCED_IN_CUSTODY("A"),
    RELEASED_ON_LICENCE("B"),
    RECALLED("C"),
    IN_CUSTODY("D"),
    IN_CUSTODY_IRC("I"),
    POST_SENTENCE_SUPERVISION("P"),
    TERMINATED("T"),
    AUTO_TERMINATED("AT"),
}

val NO_RECALL_STATUSES = listOf(CustodialStatusCode.SENTENCED_IN_CUSTODY, CustodialStatusCode.IN_CUSTODY, CustodialStatusCode.RECALLED)
val NO_CHANGE_STATUSES = listOf(CustodialStatusCode.SENTENCED_IN_CUSTODY, CustodialStatusCode.IN_CUSTODY)
val TERMINATED_STATUSES = listOf(CustodialStatusCode.TERMINATED, CustodialStatusCode.AUTO_TERMINATED)

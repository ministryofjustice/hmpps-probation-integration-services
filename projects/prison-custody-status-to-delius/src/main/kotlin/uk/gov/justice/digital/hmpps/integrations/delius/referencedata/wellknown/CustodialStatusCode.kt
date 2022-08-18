package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown

enum class CustodialStatusCode(val code: String) {
    SENTENCED_IN_CUSTODY("A"),
    RELEASED_ON_LICENCE("B"),
    RECALLED("C"),
    IN_CUSTODY("D"),
    POST_SENTENCE_SUPERVISION("P"),
}

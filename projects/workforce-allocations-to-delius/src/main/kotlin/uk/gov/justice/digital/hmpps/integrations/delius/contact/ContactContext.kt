package uk.gov.justice.digital.hmpps.integrations.delius.contact

data class ContactContext(
    val contactTypeCode: ContactTypeCode,
    val offenderId: Long,
    val eventId: Long? = null,
    val requirementId: Long? = null,
)

enum class ContactTypeCode(val value: String) {
    OFFENDER_MANAGER_TRANSFER("ETOM"),
    ORDER_SUPERVISOR_TRANSFER("ETOS"),
    SENTENCE_COMPONENT_TRANSFER("ETRC")
}

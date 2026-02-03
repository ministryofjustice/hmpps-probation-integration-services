package uk.gov.justice.digital.hmpps.api.model.contact

data class CreateContact(
    val staffId: Long,
    val contactType: String,
    val eventId: Long? = null,
    val requirementId: Long? = null,
    val description: String? = null,
    val notes: String? = null,
    val alert: Boolean,
    val sensitive: Boolean,
    val visorReport: Boolean
) {
    enum class Type(val code: String) {
        EmailTextFromOther("CM3A"),
        EmailTextFromPoP("CMOA"),
        EmailTextToOther("CM3B"),
        EmailTextToPoP("CMOB"),
        InternalCommunications("C326"),
        PhoneContactFromOther("CT3A"),
        PhoneContactFromPoP("CTOA"),
        PhoneContactToOther("CT3B"),
        PhoneContactToPoP("CTOB"),
        PoliceLiaison("C204")
    }
}

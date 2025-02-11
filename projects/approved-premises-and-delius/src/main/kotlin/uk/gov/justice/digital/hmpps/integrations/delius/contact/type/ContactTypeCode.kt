package uk.gov.justice.digital.hmpps.integrations.delius.contact.type

enum class ContactTypeCode(val code: String) {
    APPLICATION_SUBMITTED("EAPR"),
    APPLICATION_ASSESSED("EAPE"),
    APPLICATION_WITHDRAWN("EAAW"),
    BOOKING_MADE("EAPB"),
    BOOKING_CANCELLED("EABC"),
    BOOKING_CHANGED("EABA"),
    ARRIVED("EAPA"),
    NOT_ARRIVED("EAPX"),
    DEPARTED("EAPD"),
    NSI_REFERRAL("NREF"),
    NSI_TERMINATED("NTER")
}

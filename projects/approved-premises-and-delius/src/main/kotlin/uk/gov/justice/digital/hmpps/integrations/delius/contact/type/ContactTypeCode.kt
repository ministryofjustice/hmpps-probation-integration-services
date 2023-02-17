package uk.gov.justice.digital.hmpps.integrations.delius.contact.type

enum class ContactTypeCode(val code: String) {
    APPLICATION_SUBMITTED("EAPR"),
    APPLICATION_ASSESSED("EAPE"),
    BOOKING_MADE("EAPB"),
    ARRIVED("EAPA"),
    NOT_ARRIVED("EAPX"),
    DEPARTED("EAPD")
}

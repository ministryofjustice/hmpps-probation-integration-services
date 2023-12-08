package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown

enum class CustodyEventTypeCode(val code: String, val description: String) {
    STATUS_CHANGE("TSC", "Throughcare Status Change"),
    LOCATION_CHANGE("CPL", "Change Prison Location"),
}

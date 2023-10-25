package uk.gov.justice.digital.hmpps.integrations.approvedpremesis

import java.time.ZonedDateTime

data class EventDetails<T>(
    val id: String,
    val timestamp: ZonedDateTime,
    val eventType: String,
    val eventDetails: T
)

data class ApplicationSubmitted(
    val applicationId: String
) {
    val urn = "urn:hmpps:cas3:application-submitted:$applicationId"
}

data class BookingCancelled(
    val applicationId: String?,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val cancellationReason: String,
    val cancellationContext: String?
) {
    val urn = "urn:hmpps:cas3:booking-cancelled:$bookingId"
}

data class BookingProvisional(
    val applicationId: String?,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val expectedArrivedAt: ZonedDateTime,
    val notes: String
) {
    val urn = "urn:hmpps:cas3:booking-provisional:$bookingId"
}

data class BookingConfirmed(
    val applicationId: String?,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val expectedArrivedAt: ZonedDateTime,
    val notes: String
) {
    val urn = "urn:hmpps:cas3:booking-confirmed:$bookingId"
}

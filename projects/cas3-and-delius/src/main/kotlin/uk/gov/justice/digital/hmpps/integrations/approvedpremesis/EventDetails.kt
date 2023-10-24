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
)

data class BookingCancelled(
    val applicationId: String,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val cancellationReason: String,
    val cancellationContext: String?
)

data class BookingConfirmed(
    val applicationId: String,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val expectedArrivedAt: ZonedDateTime,
    val notes: String
)

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

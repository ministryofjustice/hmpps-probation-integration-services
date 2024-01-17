package uk.gov.justice.digital.hmpps.client.approvedpremises.model

import java.time.ZonedDateTime

data class EventDetails<T>(
    val id: String,
    val timestamp: ZonedDateTime,
    val eventType: String,
    val eventDetails: T
)

data class ApplicationSubmitted(
    val applicationId: String,
    val applicationUrl: String,
    val submittedAt: ZonedDateTime,
)

data class ApplicationStatusUpdated(
    val applicationId: String,
    val applicationUrl: String,
    val newStatus: ApplicationStatus,
    val updatedAt: ZonedDateTime,
)

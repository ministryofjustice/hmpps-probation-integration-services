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
    val applicationOrigin: String,
) {
    fun applicationOriginDescription(): String = ApplicationOrigin.from(applicationOrigin).description
}

data class ApplicationStatusUpdated(
    val applicationId: String,
    val applicationUrl: String,
    val newStatus: ApplicationStatus,
    val updatedAt: ZonedDateTime,
    val applicationOrigin: String,
) {
    fun applicationOriginDescription(): String = ApplicationOrigin.from(applicationOrigin).description
}

enum class ApplicationOrigin(val description: String) {
    PrisonBail("Prison Bail"),
    CourtBail("Court Bail"),
    HomeDetentionCurfew("Home Detention Curfew");

    companion object {
        fun from(value: String) = entries.single { it.name.equals(value, ignoreCase = true) }
    }
}

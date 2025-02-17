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
    val cas2v2ApplicationOrigin: String?,
) {
    fun applicationOrigin() = ApplicationOrigin.from(cas2v2ApplicationOrigin).description
}

data class ApplicationStatusUpdated(
    val applicationId: String,
    val applicationUrl: String,
    val newStatus: ApplicationStatus,
    val updatedAt: ZonedDateTime,
    val cas2v2ApplicationOrigin: String?,
) {
    fun applicationOrigin(): String = ApplicationOrigin.from(cas2v2ApplicationOrigin).description
}

enum class ApplicationOrigin(val description: String) {
    PrisonBail("Prison Bail"),
    CourtBail("Court Bail"),
    HomeDetentionCurfew("Home Detention Curfew"),
    Null("Null");

    companion object {
        fun from(value: String?): ApplicationOrigin =
            ApplicationOrigin.entries.firstOrNull { it.name.lowercase() == value?.lowercase() } ?: Null
    }
}

package uk.gov.justice.digital.hmpps.model.response

import uk.gov.justice.digital.hmpps.model.shared.Name
import java.time.ZonedDateTime

data class Homepage(
    val upcomingAppointments: List<AppointmentSummary> = emptyList(),
    val appointmentsRequiringOutcome: List<AppointmentSummary> = emptyList(),
    val appointmentsRequiringOutcomeCount: Int = 0,
) {
    data class AppointmentSummary(
        val id: Long,
        val crn: String,
        val name: Name,
        val type: String,
        val startDateTime: ZonedDateTime,
        val endDateTime: ZonedDateTime? = null,
        val location: String? = null,
        val deliusManaged: Boolean? = null,
    )
}
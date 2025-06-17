package uk.gov.justice.digital.hmpps.api.model.appointment

import uk.gov.justice.digital.hmpps.api.model.Name

data class AppointmentChecks(
    val nonWorkingDayName: String? = null,
    val isWithinOneHourOfMeetingWith: AppointmentCheck? = null,
    val overlapsWithMeetingWith: AppointmentCheck? = null,
)

data class AppointmentCheck(
    val isCurrentUser: Boolean = false,
    val appointmentIsWith: Name
)

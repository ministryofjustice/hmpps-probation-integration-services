package uk.gov.justice.digital.hmpps.api.model.appointment

import uk.gov.justice.digital.hmpps.api.model.Name

data class AppointmentChecks(
    val nonWorkingDayName: String? = null,
    val isWithinOneHourOfMeetingWith: Name? = null,
    val overlapsWithMeetingWith: Name? = null,
)

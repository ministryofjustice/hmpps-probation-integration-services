package uk.gov.justice.digital.hmpps.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jobs.unpaid-work-appointment-reminders")
class UpwAppointmentRemindersJobProperties(
    val enabled: Boolean = false,
    val provider: String? = null,
    val excludedCrns: List<String> = emptyList(),
)
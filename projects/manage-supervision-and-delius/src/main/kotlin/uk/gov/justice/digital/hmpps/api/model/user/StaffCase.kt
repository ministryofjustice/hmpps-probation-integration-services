package uk.gov.justice.digital.hmpps.api.model.user

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.Appointment
import java.time.LocalDate

data class StaffCase(
    val caseName: Name,
    val crn: String,
    val dob: LocalDate,
    val nextAppointment: Appointment? = null,
    val previousAppointment: Appointment? = null,
    val latestSentence: String? = null,
    val numberOfAdditionalSentences: Long
)

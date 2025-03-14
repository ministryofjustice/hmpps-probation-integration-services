package uk.gov.justice.digital.hmpps.api.model.appointment

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate
import java.time.ZonedDateTime

data class UserDiary(
    val size: Int,
    val page: Int,
    val totalResults: Int,
    val totalPages: Int,
    val appointments: List<UserAppointment>
)

data class UserAppointment(
    val caseName: Name,
    val id: Long,
    val crn: String,
    val dob: LocalDate,
    val latestSentence: String,
    val numberOfAdditionalSentences: Int,
    val type: String,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime? = null
)


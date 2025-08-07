package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.activity.Activity

data class Schedule(
    val personSummary: PersonSummary,
    val userSchedule: UserSchedule
)

data class UserSchedule(
    val size: Int,
    val page: Int,
    val totalResults: Int,
    val totalPages: Int,
    val appointments: List<Activity>
)
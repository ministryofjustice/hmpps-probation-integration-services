package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.activity.Activity

data class Schedule(
    val personSummary: PersonSummary,
    val appointments: List<Activity>
) 
package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.activity.Activity

data class PersonAppointment(
    val personSummary: PersonSummary,
    val appointment: Activity
)
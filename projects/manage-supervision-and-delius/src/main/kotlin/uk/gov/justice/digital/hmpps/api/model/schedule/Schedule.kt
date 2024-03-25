package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class Schedule(
    val personSummary: PersonSummary,
    val appointments: List<Appointment>
) 
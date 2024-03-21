package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class PersonAppointment(
    val personSummary: PersonSummary,
    val appointment: Appointment
)
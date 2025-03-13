package uk.gov.justice.digital.hmpps.model

import java.time.LocalDateTime

data class NextAppointmentDetails(val responsibleOfficer: ResponsibleOfficer, val futureAppointments: List<Appointment>)
data class ResponsibleOfficer(val name: Name, val telephoneNumber: String?)
data class Appointment(
    val id: Long,
    val type: CodedDescription,
    val datetime: LocalDateTime,
    val description: String?,
    val location: OfficeAddress?,
    val officer: Officer
)

data class Officer(val code: String, val name: Name)
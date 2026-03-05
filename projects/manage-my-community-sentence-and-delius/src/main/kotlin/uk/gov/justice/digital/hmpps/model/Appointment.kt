package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.appointment.Contact
import uk.gov.justice.digital.hmpps.model.Address.Companion.toModel
import java.time.LocalDate
import java.time.LocalTime

data class Appointment(
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val type: String,
    val description: String?,
    val practitioner: Practitioner,
    val location: Address?,
    val attended: Boolean?,
    val complied: Boolean?,
) {
    companion object {
        fun Contact.toAppointment() = Appointment(
            date = date,
            startTime = startTime,
            endTime = endTime,
            type = type.description,
            description = description,
            practitioner = Practitioner(staff.name()),
            location = location?.toModel(),
            attended = attended,
            complied = complied,
        )
    }
}
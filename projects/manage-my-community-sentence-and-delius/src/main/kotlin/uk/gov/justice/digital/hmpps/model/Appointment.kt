package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.appointment.Contact
import uk.gov.justice.digital.hmpps.model.OfficeAddress.Companion.toModel
import uk.gov.justice.digital.hmpps.model.UpwResponse.Companion.toModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

data class Appointment(
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val type: String,
    val practitioner: Practitioner,
    val location: OfficeAddress?,
    val attended: Boolean?,
    val complied: Boolean?,
    val outcome: String?,
    val nationalStandards: Boolean,
    val lastUpdatedAt: ZonedDateTime,
    val unpaidWork: UpwResponse?
) {
    companion object {
        fun Contact.toModel() = Appointment(
            date = date,
            startTime = startTime,
            endTime = endTime,
            type = type.description,
            practitioner = Practitioner(staff.name()),
            location = location?.toModel(),
            attended = attended,
            complied = complied,
            outcome = outcome?.description,
            nationalStandards = type.nationalStandards,
            lastUpdatedAt = lastUpdatedDatetime,
            unpaidWork = unpaidWorkAppointment?.toModel()
        )
    }
}
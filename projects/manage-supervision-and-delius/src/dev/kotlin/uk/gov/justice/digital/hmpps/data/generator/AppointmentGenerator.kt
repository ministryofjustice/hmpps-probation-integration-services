package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentType

object AppointmentGenerator {

    val APPOINTMENT_TYPES = CreateAppointment.Type.entries.map { generateType(it.code, attendanceType = true) }

    fun generateType(
        code: String,
        description: String = "Description for $code",
        attendanceType: Boolean,
        id: Long = IdGenerator.getAndIncrement()
    ) = AppointmentType(code, description, attendanceType, id)
}
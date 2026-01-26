package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Alert
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.AlertRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.PersonManagerRepository

@Service
internal class AlertService(
    private val alertRepository: AlertRepository,
    private val personManagerRepository: PersonManagerRepository,
) {
    fun createAlert(appointment: AppointmentContact) {
        val manager = personManagerRepository.getActiveManagerForPerson(appointment.personId)
        alertRepository.save(
            Alert(
                appointment = appointment,
                appointmentTypeId = appointment.type.id,
                personId = appointment.personId,
                managerId = manager.id,
                staffId = manager.staffId,
                teamId = manager.teamId
            )
        )
    }

    fun removeAlert(appointment: AppointmentContact) {
        appointment.id?.let { alertRepository.deleteByAppointmentId(appointment.id) }
    }
}
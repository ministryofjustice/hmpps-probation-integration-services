package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.Appointment
import uk.gov.justice.digital.hmpps.model.Appointment.Companion.toAppointment
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.UnpaidWorkAppointmentRepository

@Service
class AppointmentService(
    private val contactRepository: ContactRepository,
    private val personRepository: PersonRepository,
    private val upwWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
) {
    fun getFutureAppointments(crn: String, pageable: Pageable): PagedModel<Appointment> {
        val personId = personRepository.getIdByCrn(crn)
        val contacts = contactRepository.findFutureAppointments(personId, pageable)
        val upwAppointments = upwWorkAppointmentRepository.findAllByContactIdIn(contacts.map { it.id }.toList())
            .associateBy { it.contact.id }
        return PagedModel(contacts.map { it.toAppointment(upwAppointments[it.id]) })
    }

    fun getPastAppointments(crn: String, pageable: Pageable): PagedModel<Appointment> {
        val personId = personRepository.getIdByCrn(crn)
        val contacts = contactRepository.findPastAppointments(personId, pageable)
        val upwAppointments = upwWorkAppointmentRepository.findAllByContactIdIn(contacts.map { it.id }.toList())
            .associateBy { it.contact.id }
        return PagedModel(contacts.map { it.toAppointment(upwAppointments[it.id]) })
    }
}

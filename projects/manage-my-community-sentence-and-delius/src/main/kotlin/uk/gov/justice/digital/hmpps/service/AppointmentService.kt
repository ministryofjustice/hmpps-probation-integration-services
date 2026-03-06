package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Appointment
import uk.gov.justice.digital.hmpps.model.Appointment.Companion.toAppointment
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@Service
class AppointmentService(
    private val contactRepository: ContactRepository,
    private val personRepository: PersonRepository,
) {
    fun getFutureAppointments(crn: String, pageable: Pageable): PagedModel<Appointment> {
        val personId = personRepository.findIdByCrn(crn) ?: throw NotFoundException("Person", "CRN", crn)
        return PagedModel(contactRepository.findFutureAppointments(personId, pageable).map { it.toAppointment() })
    }

    fun getPastAppointments(crn: String, pageable: Pageable): PagedModel<Appointment> {
        val personId = personRepository.findIdByCrn(crn) ?: throw NotFoundException("Person", "CRN", crn)
        return PagedModel(contactRepository.findPastAppointments(personId, pageable).map { it.toAppointment() })
    }
}

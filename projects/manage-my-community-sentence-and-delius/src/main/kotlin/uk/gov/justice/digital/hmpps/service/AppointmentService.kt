package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.Appointment
import uk.gov.justice.digital.hmpps.model.Appointment.Companion.toModel
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@Service
class AppointmentService(
    private val contactRepository: ContactRepository,
    private val personRepository: PersonRepository,
) {
    fun getFutureAppointments(
        crn: String,
        excludeUnpaidWorkProjects: Set<String>,
        pageable: Pageable,
    ): PagedModel<Appointment> = PagedModel(
        contactRepository.findFutureAppointments(
            personRepository.getIdByCrn(crn),
            excludeUnpaidWorkProjects,
            pageable = pageable
        ).map { it.toModel() }
    )

    fun getPastAppointments(
        crn: String,
        excludeUnpaidWorkProjects: Set<String>,
        pageable: Pageable,
    ): PagedModel<Appointment> = PagedModel(
        contactRepository.findPastAppointments(
            personRepository.getIdByCrn(crn),
            excludeUnpaidWorkProjects,
            pageable = pageable
        ).map { it.toModel() }
    )
}

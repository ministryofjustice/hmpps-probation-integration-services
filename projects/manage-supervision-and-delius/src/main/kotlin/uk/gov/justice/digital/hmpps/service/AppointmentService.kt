package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.ContactTypeAssociation
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson

@Service
class AppointmentService(
    private val personRepository: PersonRepository,
    private val contactTypeRepository: ContactTypeRepository
) {

    fun getDetailsByContactType(crn: String, code: String): ContactTypeAssociation {
        val person = personRepository.getPerson(crn)

        if (!CreateAppointment.Type.entries.any{it.code == code}) {
            throw NotFoundException("CreateAppointment", "code", code)
        }

        val contactType = contactTypeRepository.getContactType(code)

        return ContactTypeAssociation(
            person.toSummary(),
            code,
            contactType.offenderContact
        )
    }
}
package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.ContactTypeAssociation
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.sentence.OrderSummary
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson

@Service
class AppointmentService(
    private val personRepository: PersonRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val sentenceService: SentenceService
) {

    fun getProbationRecordsByContactType(crn: String, code: String): ContactTypeAssociation {
        val person = personRepository.getPerson(crn)

        if (!CreateAppointment.Type.entries.any{it.code == code}) {
            throw NotFoundException("CreateAppointment", "code", code)
        }

        val contactType = contactTypeRepository.getContactType(code)
        val activeEvents = sentenceService.getActiveSentences(person.id)

        return ContactTypeAssociation(
            person.toSummary(),
            code,
            contactType.offenderContact,
            activeEvents.map { it.toOrderSummary() }
        )
    }
}

fun Event.toOrderSummary(): OrderSummary = OrderSummary(id, disposal?.type?.description ?: "Pre-Sentence")
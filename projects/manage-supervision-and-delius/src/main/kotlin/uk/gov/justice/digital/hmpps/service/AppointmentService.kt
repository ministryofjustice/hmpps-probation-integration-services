package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.ContactTypeAssociation
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.sentence.AssociationSummary
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository

@Service
class AppointmentService(
    private val personRepository: PersonRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val sentenceService: SentenceService,
    private val licenceConditionRepository: LicenceConditionRepository
) {

    fun getProbationRecordsByContactType(crn: String, code: String): ContactTypeAssociation {
        val person = personRepository.getPerson(crn)

        if (!CreateAppointment.Type.entries.any {it.code == code}) {
            throw NotFoundException("CreateAppointment", "code", code)
        }

        val contactType = contactTypeRepository.getContactType(code)
        val activeEvents = sentenceService.getActiveSentences(person.id)


        return ContactTypeAssociation(
            personSummary = person.toSummary(),
            contactTypeCode = code,
            associatedWithPerson = contactType.offenderContact,
            events = activeEvents.map { it.toAssociationSummary() },
            licenceConditions = activeEvents.mapNotNull {
                it.disposal?.let {
                    licenceConditionRepository.findAllByDisposalId(it.id)
                }
            }.flatten().map { it.toAssociationSummary() }
        )
    }
}


fun LicenceCondition.toAssociationSummary() = AssociationSummary(id, mainCategory.description)
fun Event.toAssociationSummary() = AssociationSummary(id, disposal?.type?.description ?: "Pre-Sentence")
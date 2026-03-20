package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.ContactRepository
import uk.gov.justice.digital.hmpps.entity.DocumentEntity.Companion.cossoBreachNoticeUrn
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.ContactResponse
import uk.gov.justice.digital.hmpps.model.FailureAndEnforcementResponse
import uk.gov.justice.digital.hmpps.model.RegistrationResponse
import java.util.UUID

@Service
class FailureAndEnforcementService(
    private val contactRepository: ContactRepository,
    private val registrationRepository: RegistrationRepository,
    private val documentRepository: DocumentRepository,
    private val personRepository: PersonRepository,
) {
    fun getFailuresAndEnforcement(crn: String, cossoId: String): FailureAndEnforcementResponse {
        val person = personRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        val document = documentRepository.findByExternalReference(cossoBreachNoticeUrn(UUID.fromString(cossoId)))
            ?: throw NotFoundException("DocumentEntity", "breachNoticeId", cossoId)
        require(document.person.id == person.id) { "Document does not relate to person with CRN $crn" }
        val eventId = documentRepository.findEventIdFromDocument(cossoBreachNoticeUrn(UUID.fromString(cossoId)))
            ?: throw NotFoundException("DocumentEntity", "breachNoticeId", cossoId)
        val registrations = registrationRepository.findRegistrationsByCrn(crn, listOf("ALT7", "ALSH"))
        val contacts = contactRepository.findEnforceableByEventId(eventId)
        return FailureAndEnforcementResponse(
            enforceableContacts = contacts.map {
                ContactResponse(
                    id = it.id,
                    datetime = it.date.atTime(it.startTime.toLocalTime()).atZone(EuropeLondon),
                    description = it.type.description,
                    type = CodeAndDescription(it.type.code, it.type.description),
                    outcome = CodeAndDescription(it.outcomeType.code, it.outcomeType.description),
                    notes = it.notes
                )
            },
            registrations = registrations.map {
                RegistrationResponse(
                    id = it.id,
                    type = CodeAndDescription(it.type.code, it.type.description),
                    level = CodeAndDescription(it.registerLevel.code, it.registerLevel.description),
                    category = CodeAndDescription(it.registerCategory.code, it.registerCategory.description),
                    startDate = it.startDate,
                    endDate = it.deregistration?.date,
                    notes = it.notes,
                    documentsLinked = it.documentLinked,
                    deregistered = it.deregistered,
                )
            }
        )
    }
}
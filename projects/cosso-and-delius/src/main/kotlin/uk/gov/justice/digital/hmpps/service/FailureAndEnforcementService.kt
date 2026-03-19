package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
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
class FailureAndEnforcementService (
    private val contactRepository: ContactRepository,
    private val registrationRepository: RegistrationRepository,
    private val documentRepository: DocumentRepository,
    private val personRepository: PersonRepository,
) {
    fun getFailuresAndEnforcement(crn: String, cossoId: String): FailureAndEnforcementResponse {
        personRepository.findByCrn(crn) ?: throw NotFoundException("PersonEntity", "crn", crn)
        val eventId = documentRepository.findEventIdFromDocument(cossoBreachNoticeUrn(UUID.fromString(cossoId)))
            ?: throw NotFoundException("DocumentEntity", "breachNoticeId", cossoId)
        val registrations = registrationRepository.findRegistrationsByCrn(crn, listOf("ALT7", "ALSH"))
        val contacts = contactRepository.findEnforceableByEventId(eventId)
        return FailureAndEnforcementResponse(
            enforceableContacts = contacts.map { ContactResponse(
                id = it.id,
                datetime = it.contactStartTime,
                description = it.contactType.description,
                type = CodeAndDescription(it.contactType.code, it.contactType.description),
                outcome = CodeAndDescription(it.contactOutcomeType.code, it.contactOutcomeType.description),
                notes = it.notes
            ) },
            registrations = registrations.map { RegistrationResponse(
                id = it.id,
                type = CodeAndDescription(it.registerType.code, it.registerType.description),
                level = CodeAndDescription(it.registerLevel.code, it.registerLevel.description),
                category = CodeAndDescription(it.registerCategory.code, it.registerCategory.description),
                startDate = it.startDate,
                endData = it.endDate,
                notes = it.registrationNotes,
                documentsLinked = it.documentLinked,
                deregistered = it.deregistered,
            )
            }
        )
    }
}
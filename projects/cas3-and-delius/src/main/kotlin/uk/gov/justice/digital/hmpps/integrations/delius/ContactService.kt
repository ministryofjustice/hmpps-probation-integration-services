package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.Cas3Event
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.EventDetails
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrn
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class ContactService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val telemetryService: TelemetryService
) : AuditableService(auditedInteractionService) {

    fun <T : Cas3Event> createContact(
        crn: String,
        person: Person? = null,
        getEvent: () -> EventDetails<T>
    ) = audit(BusinessInteractionCode.UPDATE_CONTACT) {
        val event = getEvent()
        val personId = person?.id ?: personRepository.getByCrn(crn).id
        val existing = contactRepository.getByExternalReference(event.eventDetails.urn)
        if (existing != null) {
            if (existing.startTime < event.timestamp) {
                existing.notes = event.eventDetails.noteText
                existing.date = event.timestamp.toLocalDate()
                existing.startTime = event.timestamp
                contactRepository.save(existing)
            } else {
                telemetryService.trackEvent(
                    "Ignoring out of sequence older message.",
                    mapOf("crn" to crn, "urn" to event.eventDetails.urn)
                )
            }
        } else {
            contactRepository.save(
                newContact(
                    event.timestamp,
                    personId,
                    event.eventDetails.contactTypeCode,
                    event.eventDetails.urn,
                    event.eventDetails.noteText
                )
            )
        }
    }

    fun newContact(
        occurredAt: ZonedDateTime,
        personId: Long,
        typeCode: String,
        reference: String,
        notes: String
    ): Contact {
        val contactType = contactTypeRepository.findByCode(typeCode) ?: throw NotFoundException(
            "ContactType",
            "code",
            typeCode
        )
        val comDetails = personManagerRepository.findActiveManager(personId) ?: throw NotFoundException(
            "PersonManager",
            "personId",
            personId
        )

        return Contact(
            offenderId = personId,
            type = contactType,
            notes = notes,
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            isSensitive = contactType.isSensitive,
            probationAreaId = comDetails.probationAreaId,
            teamId = comDetails.teamId,
            staffId = comDetails.staffId,
            externalReference = reference
        )
    }
}

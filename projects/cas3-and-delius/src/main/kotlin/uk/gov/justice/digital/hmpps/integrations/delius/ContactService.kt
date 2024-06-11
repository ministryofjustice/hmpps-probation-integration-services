package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.By
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.Cas3Event
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.Recordable
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class ContactService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val providerService: ProviderService,
    private val telemetryService: TelemetryService
) : AuditableService(auditedInteractionService) {
    fun <T : Cas3Event> createOrUpdateContact(
        crn: String,
        person: Person? = null,
        replaceNotes: Boolean = true,
        extraInfo: String? = null,
        getEvent: () -> EventDetails<T>
    ) = audit(BusinessInteractionCode.UPDATE_CONTACT) {
        val event = getEvent()
        val personId = person?.id ?: personRepository.getByCrn(crn).id
        val existing = contactRepository.getByExternalReference(event.eventDetails.urn)
        if (existing != null) {
            if (existing.lastModifiedDateTime < event.timestamp) {
                if (replaceNotes) {
                    existing.notes = event.eventDetails.noteText
                } else {
                    existing.notes = listOfNotNull(
                        existing.notes,
                        extraInfo,
                        event.eventDetails.noteText
                    ).joinToString(System.lineSeparator())
                }
                existing.date = event.occurredAt().toLocalDate()
                existing.startTime = event.occurredAt()
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
                    event.occurredAt(),
                    personId,
                    event.eventDetails.contactTypeCode,
                    event.eventDetails.urn,
                    event.eventDetails.noteText,
                    if (event.eventDetails is Recordable) event.eventDetails.recordedBy else null,
                )
            )
        }
    }

    fun newContact(
        occurredAt: ZonedDateTime,
        personId: Long,
        typeCode: String,
        reference: String,
        notes: String,
        by: By?
    ): Contact {
        val contactType = contactTypeRepository.findByCode(typeCode) ?: throw NotFoundException(
            "ContactType",
            "code",
            typeCode
        )

        val managerIds =
            by?.let { providerService.findManagerIds(it) } ?: personManagerRepository.getActiveManager(personId)

        return Contact(
            offenderId = personId,
            type = contactType,
            notes = notes,
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            isSensitive = contactType.isSensitive,
            probationAreaId = managerIds.probationAreaId,
            teamId = managerIds.teamId,
            staffId = managerIds.staffId,
            externalReference = reference
        )
    }
}

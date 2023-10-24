package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.Cas3ApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType.Companion.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.url
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class ContactService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val telemetryService: TelemetryService,
    private val cas3ApiClient: Cas3ApiClient
) : AuditableService(auditedInteractionService) {

    fun createReferralSubmitted(event: HmppsDomainEvent) = audit(BusinessInteractionCode.UPDATE_CONTACT) {
        val details = cas3ApiClient.getApplicationSubmittedDetails(event.url()).eventDetails
        val crn = event.personReference.findCrn()
        val externalReference = details.applicationId
        val person = personRepository.getByCrn(crn!!)

        if (contactRepository.getByExternalReference(externalReference) != null) {
            telemetryService.trackEvent("Duplicate ApplicationSubmitted event received for crn $crn")
        } else {
            contactRepository.save(newContact(event.occurredAt, person.id, REFERRAL_SUBMITTED, externalReference))
        }
    }

    fun newContact(occurredAt: ZonedDateTime, personId: Long, typeCode: String, reference: String): Contact {
        val contactType = contactTypeRepository.findByCode(REFERRAL_SUBMITTED) ?: throw NotFoundException(
            "ContactType",
            "code",
            REFERRAL_SUBMITTED
        )
        val comDetails = personManagerRepository.findActiveManager(personId) ?: throw NotFoundException(
            "PersonManager",
            "personId",
            personId
        )

        return Contact(
            offenderId = personId,
            type = contactType,
            notes = "",
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            isSensitive = contactType.isSensitive,
            probationAreaId = comDetails.probationAreaId,
            teamId = comDetails.teamId,
            staffId = comDetails.staffId,
            staffEmployeeId = comDetails.staffEmployeeId,
            externalReference = reference
        )
    }
}

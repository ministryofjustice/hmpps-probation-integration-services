package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType.Companion.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByNoms
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
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

    fun createReferralSubmitted(event: HmppsDomainEvent) = audit(BusinessInteractionCode.UPDATE_CONTACT) {
        val crn = event.personReference.findCrn()
        val noms = event.personReference.findNomsNumber()
        val externalReference = event.additionalInformation["applicationId"] as String

        val person = when {
            crn != null -> {
                personRepository.getByCrn(crn)
            }

            noms != null -> {
                personRepository.getByNoms(noms)
            }

            else -> {
                throw IllegalArgumentException("crn or noms number should be supplied in the message")
            }
        }
        if (contactRepository.getByExternalReference(externalReference) != null) {
            telemetryService.trackEvent("Duplicate ApplicationSubmitted event received for crn/noms ${crn}/${noms}")
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
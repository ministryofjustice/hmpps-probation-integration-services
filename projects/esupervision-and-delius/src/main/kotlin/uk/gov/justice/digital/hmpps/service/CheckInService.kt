package uk.gov.justice.digital.hmpps.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.entity.ContactOutcome.Companion.SETUP_COMPLETED
import uk.gov.justice.digital.hmpps.entity.ContactOutcome.Companion.SETUP_REMOVED
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.E_SUPERVISION_CHECK_IN
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.E_SUPERVISION_SETUP_COMPLETED
import uk.gov.justice.digital.hmpps.entity.audit.BusinessInteractionCode.ADD_CONTACT
import uk.gov.justice.digital.hmpps.entity.audit.BusinessInteractionCode.UPDATE_CONTACT
import uk.gov.justice.digital.hmpps.entity.event.EventRepository
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException.Companion.orIgnore
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.*
import uk.gov.justice.digital.hmpps.messaging.detail.CheckInDetail

@Service
@Transactional
class CheckInService(
    auditedInteractionService: AuditedInteractionService,
    private val domainEventDetailService: DomainEventDetailService,
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val contactRepository: ContactRepository,
    private val personRepository: PersonRepository,
    @Value("\${sentry.environment}") private val env: String,
) : AuditableService(auditedInteractionService) {
    fun receiveCheckIn(domainEvent: HmppsDomainEvent) = audit(ADD_CONTACT) { audit ->
        if (!personRepository.existsByCrn(domainEvent.crn)) throw IgnorableMessageException("CRN not found")
        contactRepository.save(domainEvent.checkInContact()).also { audit(it) }
    }

    fun updateCheckIn(domainEvent: HmppsDomainEvent) = audit(UPDATE_CONTACT) { audit ->
        if (!personRepository.existsByCrn(domainEvent.crn)) throw IgnorableMessageException("CRN not found")
        val detail = requireNotNull(domainEvent.getDetail())
        val externalReferences = Contact.checkInExternalReferencePrefixes.map { it + detail.checkinUuid }
        val contact = contactRepository.getByExternalReferenceIn(externalReferences)
        require(domainEvent matches contact) { "Case details mismatch" }
        contact.notes = listOfNotNull(contact.notes, detail.notes).joinToString(System.lineSeparator())
        contact.isSensitive = detail.sensitive || contact.isSensitive == true
        contactRepository.save(contact).also { audit(it) }
    }

    fun completeSetup(domainEvent: HmppsDomainEvent) = audit(ADD_CONTACT) { audit ->
        if (!personRepository.existsByCrn(domainEvent.crn)) throw IgnorableMessageException("CRN not found")
        contactRepository.save(domainEvent.setupCompletedContact()).also { audit(it) }
    }

    fun removeSetup(domainEvent: HmppsDomainEvent) = audit(UPDATE_CONTACT) { audit ->
        if (!personRepository.existsByCrn(domainEvent.crn)) throw IgnorableMessageException("CRN not found")
        val contact = if (domainEvent.setupId != null) {
            val externalReference = Contact.externalReferencePrefix(domainEvent.eventType) + domainEvent.setupId
            contactRepository.findByExternalReference(externalReference) ?: when (env) {
                "dev" -> throw IgnorableMessageException("Setup not found")
                else -> throw NotFoundException("Contact", "externalReference", externalReference)
            }
        } else {
            val eventNumber = requireNotNull(domainEvent.eventNumber) { "No setupId or eventNumber" }
            contactRepository.findByPersonCrnAndEventNumberAndTypeCode(domainEvent.crn, eventNumber)
                .orIgnore { "Setup not found" }
        }
        if (contact.event == null) throw IgnorableMessageException("Event not found for setup removal")
        require(domainEvent matches contact) { "Case details mismatch" }
        val outcomeCode = domainEvent.additionalInformation["outcomeCode"]?.toString()?.takeIf { it.isNotBlank() } ?: SETUP_REMOVED
        contact.outcome = contactOutcomeRepository.getByCode(outcomeCode)
        contactRepository.save(contact).also { audit(it) }
    }

    private fun HmppsDomainEvent.getEvent() = eventNumber?.let { eventRepository.findByPersonCrnAndNumber(crn, it) }
        ?: eventRepository.findFirstByPersonCrnAndActiveTrueOrderByReferralDateDesc(crn)
        ?: error("Case does not have an active event")

    private fun HmppsDomainEvent.getDetail() =
        detailUrl?.let { domainEventDetailService.getDetail<CheckInDetail>(this) }

    private fun HmppsDomainEvent.checkInContact(): Contact {
        val detail = detailUrl?.let { domainEventDetailService.getDetail<CheckInDetail>(it) }
        val manager = personManagerRepository.getByCrn(crn)
        return Contact(
            person = manager.person,
            event = getEvent(),
            type = contactTypeRepository.getByCode(E_SUPERVISION_CHECK_IN),
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            provider = manager.provider,
            team = manager.team,
            staff = manager.staff,
            description = description(),
            notes = listOfNotNull(
                checkInUrl?.let { "Review the online check in using the manage probation check ins service: $it" },
                detail?.notes
            ).joinToString(System.lineSeparator()),
            isSensitive = detail?.sensitive ?: false,
            externalReference = detail?.checkinUuid?.let { Contact.externalReferencePrefix(eventType) + it },
        )
    }

    private fun HmppsDomainEvent.setupCompletedContact(): Contact {
        val manager = personManagerRepository.getByCrn(crn)
        return Contact(
            person = manager.person,
            event = getEvent(),
            type = contactTypeRepository.getByCode(E_SUPERVISION_SETUP_COMPLETED),
            outcome = contactOutcomeRepository.getByCode(SETUP_COMPLETED),
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            provider = manager.provider,
            team = manager.team,
            staff = manager.staff,
            externalReference = setupId?.let { Contact.externalReferencePrefix(eventType) + it }
        )
    }

    private operator fun AuditedInteraction.Parameters.invoke(contact: Contact) {
        this["contactId"] = contact.id
        this["offenderId"] = contact.person.id
        this["eventId"] = checkNotNull(contact.event).id
    }

    private infix fun HmppsDomainEvent.matches(contact: Contact) =
        contact.person.crn == crn && (eventNumber == null || contact.event?.number == eventNumber)
}

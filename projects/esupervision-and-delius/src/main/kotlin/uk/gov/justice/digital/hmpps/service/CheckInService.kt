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
    fun receiveCheckIn(domainEvent: HmppsDomainEvent) {
        val detail = domainEvent.getDetail()
        val contact = detail?.let {
            val externalReferences = Contact.checkInExternalReferencePrefixes.map { prefix -> prefix + it.checkinUuid }
            contactRepository.findByExternalReferenceIn(externalReferences)
        }

        if (contact == null) {
            audit(ADD_CONTACT) { audit ->
                checkCrnExists(domainEvent.crn)
                contactRepository.save(domainEvent.checkInContact("Review the online check in using the manage probation check ins service"))
                    .also { audit(it) }
            }
        } else {
            audit(UPDATE_CONTACT) { audit ->
                checkCrnExists(domainEvent.crn)
                require(domainEvent matches contact) { "Case details mismatch" }
                contact.updateFromCheckIn(domainEvent, detail)
                contactRepository.save(contact).also { audit(it) }
            }
        }
    }

    fun updateCheckIn(domainEvent: HmppsDomainEvent) = audit(UPDATE_CONTACT) { audit ->
        checkCrnExists(domainEvent.crn)
        val detail = requireNotNull(domainEvent.getDetail())
        val externalReferences = Contact.checkInExternalReferencePrefixes.map { it + detail.checkinUuid }
        val contact = contactRepository.getByExternalReferenceIn(externalReferences)
        require(domainEvent matches contact) { "Case details mismatch" }
        val rescheduled = "Check in status: Rescheduled"
        detail.dateTime?.let { dateTime ->
            contact.date = dateTime.toLocalDate()
            contact.startTime = dateTime
        }
        contact.notes = listOfNotNull(
            contact.notes,
            detail.dateTime?.let { rescheduled },
            detail.notes
        ).joinToString(System.lineSeparator())
        contact.description = domainEvent.description()
        contact.isSensitive = detail.sensitive || contact.isSensitive == true
        contactRepository.save(contact).also { audit(it) }
    }

    fun checkInScheduled(domainEvent: HmppsDomainEvent) = audit(ADD_CONTACT) { audit ->
        checkCrnExists(domainEvent.crn)
        contactRepository.save(domainEvent.checkInContact("Check in status: Scheduled")).also { audit(it) }
    }

    fun checkInUpdateNotes(domainEvent: HmppsDomainEvent, notes: String) = audit(UPDATE_CONTACT) { audit ->
        checkCrnExists(domainEvent.crn)
        val detail = requireNotNull(domainEvent.getDetail())
        val externalReferences = Contact.checkInExternalReferencePrefixes.map { it + detail.checkinUuid }
        val contact = contactRepository.getByExternalReferenceIn(externalReferences)
        require(domainEvent matches contact) { "Case details mismatch" }
        contact.notes = listOfNotNull(
            contact.notes,
            notes,
            detail.notes
        ).joinToString(System.lineSeparator())
        contact.isSensitive = detail.sensitive || contact.isSensitive == true
        contact.description = domainEvent.description()
        contactRepository.save(contact).also { audit(it) }
    }

    fun completeSetup(domainEvent: HmppsDomainEvent) = audit(ADD_CONTACT) { audit ->
        checkCrnExists(domainEvent.crn)
        contactRepository.save(domainEvent.setupCompletedContact()).also { audit(it) }
    }

    fun removeSetup(domainEvent: HmppsDomainEvent) = audit(UPDATE_CONTACT) { audit ->
        checkCrnExists(domainEvent.crn)
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
        contact.outcome = contactOutcomeRepository.getByCode(domainEvent.outcomeCode)
        contactRepository.save(contact).also { audit(it) }
    }

    private fun HmppsDomainEvent.getEvent() = eventNumber?.let { eventRepository.findByPersonCrnAndNumber(crn, it) }
        ?: eventRepository.findFirstByPersonCrnAndActiveTrueOrderByReferralDateDesc(crn)
        ?: error("Case does not have an active event")

    private fun HmppsDomainEvent.getDetail() =
        detailUrl?.let { domainEventDetailService.getDetail<CheckInDetail>(this) }

    private fun Contact.updateFromCheckIn(domainEvent: HmppsDomainEvent, detail: CheckInDetail) {
        val checkInDateTime = detail.dateTime
        date = checkInDateTime?.toLocalDate() ?: domainEvent.occurredAt.toLocalDate()
        startTime = checkInDateTime ?: domainEvent.occurredAt
        description = domainEvent.description()
        notes = listOfNotNull(
            domainEvent.checkInUrl?.let { "Review the online check in using the manage probation check ins service: $it" },
            detail.notes
        ).joinToString(System.lineSeparator())
        isSensitive = detail.sensitive || isSensitive == true
    }

    private fun HmppsDomainEvent.checkInContact(notes: String): Contact {
        val detail = detailUrl?.let { domainEventDetailService.getDetail<CheckInDetail>(it) }
        val manager = personManagerRepository.getByCrn(crn)
        val checkInDateTime = detail?.dateTime
        return Contact(
            person = manager.person,
            event = getEvent(),
            type = contactTypeRepository.getByCode(E_SUPERVISION_CHECK_IN),
            date = checkInDateTime?.toLocalDate() ?: occurredAt.toLocalDate(),
            startTime = checkInDateTime ?: occurredAt,
            provider = manager.provider,
            team = manager.team,
            staff = manager.staff,
            description = description(),
            notes = listOfNotNull(
                checkInUrl?.let { "$notes: $it" },
                detail?.notes
            ).joinToString(System.lineSeparator()),
            isSensitive = detail?.sensitive ?: false,
            externalReference = detail?.checkinUuid?.let { Contact.externalReferencePrefix(eventType) + it },
        )
    }

    private fun checkCrnExists(crn: String) {
        if (!personRepository.existsByCrn(crn)) throw IgnorableMessageException("CRN not found")
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

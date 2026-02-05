package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.integrations.delius.ContactType.Companion.E_SUPERVISION_CHECK_IN
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.ADD_CONTACT
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.UPDATE_CONTACT
import uk.gov.justice.digital.hmpps.integrations.esupervision.CheckInDetail
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.checkInUrl
import uk.gov.justice.digital.hmpps.messaging.description

@Service
@Transactional
class CheckInService(
    auditedInteractionService: AuditedInteractionService,
    private val deDetailService: DomainEventDetailService,
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val personRepository: PersonRepository,
) : AuditableService(auditedInteractionService) {
    fun handle(de: HmppsDomainEvent) = audit(ADD_CONTACT) { audit ->
        val detail = de.detailUrl?.let { deDetailService.getDetail<CheckInDetail>(de) }
        val crn = requireNotNull(de.personReference.findCrn())
        val com = personManagerRepository.getByCrn(crn)
        audit["offenderId"] = com.person.id
        val event = eventRepository.findFirstByPersonCrnOrderByReferralDateDesc(crn)
            ?: throw IllegalStateException("Case does not have an active event")
        audit["eventId"] = event.id
        val contact = contactRepository.save(de.createContact(com, event, detail))
        audit["contactId"] = contact.id
    }

    fun update(de: HmppsDomainEvent) = audit(UPDATE_CONTACT) { audit ->
        if (!personRepository.existsByCrn(requireNotNull(de.personReference.findCrn())))
            throw IgnorableMessageException("CRN not found")
        val detail = de.detailUrl?.let { deDetailService.getDetail<CheckInDetail>(de) }
        val uuid = requireNotNull(detail?.checkinUuid)
        val contact = contactRepository.getByExternalReference(Contact.externalReferencePrefix(de.eventType) + uuid)
        audit["contactId"] = contact.id
        contact.notes = listOfNotNull(
            contact.notes,
            detail.notes
        ).joinToString(System.lineSeparator())
        contactRepository.save(contact)
    }

    private fun HmppsDomainEvent.createContact(com: PersonManager, event: Event, detail: CheckInDetail?): Contact =
        Contact(
            person = com.person,
            event = event,
            type = contactTypeRepository.getByCode(E_SUPERVISION_CHECK_IN),
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            provider = com.provider,
            team = com.team,
            staff = com.staff,
            description = description(),
            notes = listOfNotNull(
                checkInUrl()?.let { "Review the online check in using the manage probation check ins service: $it" },
                detail?.notes
            ).joinToString(System.lineSeparator()),
            externalReference = detail?.checkinUuid?.let { Contact.externalReferencePrefix(eventType) + it },
            softDeleted = false,
            id = 0,
        )
}
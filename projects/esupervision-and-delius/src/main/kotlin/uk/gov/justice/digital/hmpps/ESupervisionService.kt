package uk.gov.justice.digital.hmpps

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.ADD_CONTACT
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.checkInUrl
import uk.gov.justice.digital.hmpps.messaging.description

@Transactional
@Service
class ESupervisionService(
    auditedInteractionService: AuditedInteractionService,
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
) : AuditableService(auditedInteractionService) {
    fun handle(de: HmppsDomainEvent) = audit(ADD_CONTACT) { audit ->
        val crn = requireNotNull(de.personReference.findCrn())
        val com = personManagerRepository.getByCrn(crn)
        audit["offenderId"] = com.person.id
        val event = eventRepository.findFirstByPersonCrnOrderByReferralDateDesc(crn)
            ?: throw IllegalStateException("Case does not have an active event")
        audit["eventId"] = event.id
        val contact = contactRepository.save(de.createContact(com, event))
        audit["contactId"] = contact.id
    }

    private fun HmppsDomainEvent.createContact(com: PersonManager, event: Event): Contact = Contact(
        com.person,
        event,
        contactTypeRepository.getByCode(ContactType.E_SUPERVISION_CHECK_IN),
        occurredAt.toLocalDate(),
        occurredAt,
        com.provider,
        com.team,
        com.staff,
        description(),
        "Please follow this link to review the check-in in the E-supervision portal: ${checkInUrl()}",
        false,
        0
    )
}
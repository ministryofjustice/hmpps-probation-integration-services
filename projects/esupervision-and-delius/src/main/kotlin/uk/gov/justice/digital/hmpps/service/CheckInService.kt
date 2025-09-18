package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.integrations.delius.ContactType.Companion.E_SUPERVISION_CHECK_IN
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.ADD_CONTACT
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.checkInUrl
import uk.gov.justice.digital.hmpps.messaging.description

@Service
@Transactional
class CheckInService(
    auditedInteractionService: AuditedInteractionService,
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val contactAlertRepository: ContactAlertRepository,
) : AuditableService(auditedInteractionService) {
    fun handle(de: HmppsDomainEvent) = audit(ADD_CONTACT) { audit ->
        val crn = requireNotNull(de.personReference.findCrn())
        val com = personManagerRepository.getByCrn(crn)
        audit["offenderId"] = com.person.id
        val event = eventRepository.findFirstByPersonCrnOrderByReferralDateDesc(crn)
            ?: throw IllegalStateException("Case does not have an active event")
        audit["eventId"] = event.id
        val contact = contactRepository.save(de.createContact(com, event))
        contactAlertRepository.save(contact.toAlert(com))
        audit["contactId"] = contact.id
    }

    private fun HmppsDomainEvent.createContact(com: PersonManager, event: Event): Contact = Contact(
        person = com.person,
        event = event,
        type = contactTypeRepository.getByCode(E_SUPERVISION_CHECK_IN),
        date = occurredAt.toLocalDate(),
        startTime = occurredAt,
        provider = com.provider,
        team = com.team,
        staff = com.staff,
        description = description(),
        notes = "Review the online check in using the manage probation check ins service: ${checkInUrl()}",
        softDeleted = false,
        id = 0
    )

    private fun Contact.toAlert(com: PersonManager): ContactAlert = ContactAlert(
        contactId = id,
        typeId = type.id,
        personId = person.id,
        teamId = team.id,
        staffId = staff.id,
        personManagerId = com.id
    )
}
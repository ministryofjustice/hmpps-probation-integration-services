package uk.gov.justice.digital.hmpps.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
@Transactional
class ContactService(
    auditedInteractionService: AuditedInteractionService,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val telemetryService: TelemetryService,
) : AuditableService(auditedInteractionService) {

    fun createContact(
        personId: Long,
        type: String,
        date: ZonedDateTime,
        manager: PersonManager,
        notes: String,
        urn: String,
    ) = if (contactRepository.existsByExternalReference(urn)) {
        telemetryService.trackEvent("ContactAlreadyExists", mapOf("urn" to urn))
        false
    } else audit(BusinessInteractionCode.ADD_CONTACT) { audit ->
        val contact = contactRepository.save(
            Contact(
                personId = personId,
                type = contactTypeRepository.getByCode(ContactType.REFERRAL_SUBMITTED),
                date = date.toLocalDate(),
                startTime = date,
                staffId = manager.staffId,
                teamId = manager.teamId,
                probationAreaId = manager.probationAreaId,
                notes = notes,
                externalReference = urn,
            )
        )
        audit["contactId"] = contact.id
        audit["offenderId"] = personId
        true
    }
}
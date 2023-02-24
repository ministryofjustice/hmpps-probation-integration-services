package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.MANAGE_NSI
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.NSI_TERMINATED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus.Code.END
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.integrations.delius.referral.getByCrnAndExternalReference
import uk.gov.justice.digital.hmpps.integrations.delius.referral.nsiOutcome
import uk.gov.justice.digital.hmpps.messaging.NsiTermination

@Service
class NsiService(
    auditedInteractionService: AuditedInteractionService,
    private val nsiRepository: NsiRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiOutcomeRepository: NsiOutcomeRepository,
    private val statusHistoryRepository: NsiStatusHistoryRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository
) : AuditableService(auditedInteractionService) {
    @Transactional
    fun terminateNsi(termination: NsiTermination) = audit(MANAGE_NSI) {
        val nsi = nsiRepository.getByCrnAndExternalReference(termination.crn, termination.urn)
        val status = nsiStatusRepository.nsiOutcome(END.value)
        val outcome = nsiOutcomeRepository.nsiOutcome(termination.endType.outcome)

        nsi.status = status
        nsi.outcome = outcome
        nsi.notes = listOfNotNull(nsi.notes, termination.notes).joinToString(System.lineSeparator())
        statusHistoryRepository.save(nsi.statusHistory())

        contactRepository.deleteFutureAppointmentsForNsi(nsi.id)
        contactRepository.save(nsi.statusChangeContact())
        contactRepository.save(nsi.terminationContact())
    }

    private fun Nsi.statusHistory() = NsiStatusHistory(id, status.id, statusDate, notes)
    private fun Nsi.statusChangeContact() = Contact(
        person,
        contactTypeRepository.getReferenceById(status.contactTypeId),
        eventId = eventId,
        requirementId = requirementId,
        nsiId = id,
        date = statusDate,
        providerId = manager.providerId,
        teamId = manager.teamId,
        staffId = manager.staffId
    )

    private fun Nsi.terminationContact() = Contact(
        person,
        contactTypeRepository.getByCode(NSI_TERMINATED.value),
        providerId = manager.providerId,
        teamId = manager.teamId,
        staffId = manager.staffId,
        eventId = eventId,
        requirementId = requirementId,
        nsiId = id,
        date = statusDate,
        notes = "NSI Terminated with Outcome: ${outcome!!.description}"
    )
}

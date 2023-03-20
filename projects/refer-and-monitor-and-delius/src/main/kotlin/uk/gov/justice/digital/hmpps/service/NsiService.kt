package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.MANAGE_NSI
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.NSI_COMMENCED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.NSI_REFERRAL
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.NSI_TERMINATED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus.Code.END
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.integrations.delius.referral.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referral.getByCrnAndExternalReference
import uk.gov.justice.digital.hmpps.integrations.delius.referral.nsiOutcome
import uk.gov.justice.digital.hmpps.messaging.NsiTermination
import java.time.ZonedDateTime

@Service
class NsiService(
    auditedInteractionService: AuditedInteractionService,
    private val nsiRepository: NsiRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiOutcomeRepository: NsiOutcomeRepository,
    private val statusHistoryRepository: NsiStatusHistoryRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val createNsi: CreateNsi
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun startNsi(crn: String, rs: ReferralStarted) = audit(MANAGE_NSI) { audit ->
        val nsi = nsiRepository.findByPersonCrnAndExternalReference(crn, rs.urn)
            ?: createNsi.new(crn, rs).apply {
                statusHistoryRepository.save(statusHistory())
                contactRepository.save(contact(NSI_REFERRAL.value, referralDate))
                contactRepository.save(statusChangeContact())
                contactRepository.save(contact(NSI_COMMENCED.value, actualStartDate!!))
                audit["offender_id"] = person.id
                audit["nsiId"] = id
            }

        if (nsi.notes != rs.notes) {
            nsi.notes = rs.notes
        }

        if (nsi.actualStartDate != rs.startedAt) {
            nsi.actualStartDate = rs.startedAt
        }

        if (nsi.status.code != NsiStatus.Code.IN_PROGRESS.value) {
            nsi.status = nsiStatusRepository.getByCode(NsiStatus.Code.IN_PROGRESS.value)
        }
        if (nsi.statusDate != rs.startedAt) {
            nsi.statusDate = rs.startedAt
        }
    }

    @Transactional
    fun terminateNsi(termination: NsiTermination) = audit(MANAGE_NSI) {
        val nsi = nsiRepository.getByCrnAndExternalReference(termination.crn, termination.urn)
        val status = nsiStatusRepository.getByCode(END.value)
        val outcome = nsiOutcomeRepository.nsiOutcome(termination.endType.outcome)

        it["offender_id"] = nsi.person.id
        it["nsiId"] = nsi.id

        if (nsi.status.id != status.id) {
            nsi.status = status
            nsi.statusDate = termination.endDate
            nsi.notes = listOfNotNull(nsi.notes, termination.notes).joinToString(System.lineSeparator())
            statusHistoryRepository.save(nsi.statusHistory())
            contactRepository.deleteFutureAppointmentsForNsi(nsi.id)
            contactRepository.save(nsi.statusChangeContact())
        }
        if (nsi.outcome?.id != outcome.id) {
            nsi.outcome = outcome
            contactRepository.save(nsi.terminationContact())
        }
        if (nsi.actualEndDate != termination.endDate) {
            nsi.actualEndDate = termination.endDate
        }
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

    private fun Nsi.contact(type: String, date: ZonedDateTime) = Contact(
        person,
        contactTypeRepository.getByCode(type),
        providerId = manager.providerId,
        teamId = manager.teamId,
        staffId = manager.staffId,
        eventId = eventId,
        requirementId = requirementId,
        nsiId = id,
        date = date
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

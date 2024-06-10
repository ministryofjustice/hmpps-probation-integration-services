package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.ReferralNotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.MANAGE_NSI
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.*
import uk.gov.justice.digital.hmpps.integrations.delius.contact.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.nsiOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus.Code.END
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.integrations.delius.referral.getByCode
import uk.gov.justice.digital.hmpps.messaging.NsiTermination
import java.time.ZonedDateTime

@Service
class NsiService(
    auditedInteractionService: AuditedInteractionService,
    private val nsiRepository: NsiRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiOutcomeRepository: ReferenceDataRepository,
    private val statusHistoryRepository: NsiStatusHistoryRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val createNsi: CreateNsi,
    private val featureFlags: FeatureFlags,
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun startNsi(crn: String, rs: ReferralStarted) = audit(MANAGE_NSI) { audit ->
        val find = { nsiRepository.findByPersonCrnAndExternalReference(crn, rs.urn) }
        val nsi = find()
            ?: createNsi.new(crn, rs) {
                statusHistoryRepository.save(it.statusHistory())
                contactRepository.save(it.contact(NSI_REFERRAL.value, it.statusDate))
                contactRepository.save(it.statusChangeContact())
                contactRepository.save(it.contact(NSI_COMMENCED.value, it.actualStartDate!!))
            } ?: find() ?: throw IllegalStateException("Unable to find or create NSI for ${rs.urn}")

        audit["offenderId"] = nsi.person.id
        audit["nsiId"] = nsi.id

        if (nsi.notes != rs.notes) {
            nsi.notes = rs.notes
        }

        if (nsi.actualStartDate != rs.startedAt) {
            nsi.actualStartDate = rs.startedAt
        }

        if (nsi.status.code != NsiStatus.Code.IN_PROGRESS.value) {
            nsi.status = nsiStatusRepository.getByCode(NsiStatus.Code.IN_PROGRESS.value)
            nsi.statusDate = rs.startedAt
            statusHistoryRepository.save(nsi.statusHistory())
            contactRepository.save(nsi.statusChangeContact())
        }
    }

    @Transactional
    fun terminateNsi(termination: NsiTermination) = audit(MANAGE_NSI) { audit ->
        val nsi = findNsi(termination)
        val status = nsiStatusRepository.getByCode(END.value)
        val outcomeCode = termination.withdrawalCode.takeIf { featureFlags.enabled("referral-withdrawal-reason") }
            ?: termination.endType.outcome
        val outcome = nsiOutcomeRepository.nsiOutcome(outcomeCode)

        audit["offenderId"] = nsi.person.id
        audit["nsiId"] = nsi.id

        if (nsi.status.id != status.id) {
            nsi.status = status
            nsi.statusDate = termination.endDate
            nsi.notes = listOfNotNull(nsi.notes, termination.notes).joinToString(System.lineSeparator())
            statusHistoryRepository.save(nsi.statusHistory())
            contactRepository.withdrawFutureAppointments(
                nsi.id,
                contactOutcomeRepository.getByCode(ContactOutcome.Code.WITHDRAWN.value)
            )
            contactRepository.save(nsi.statusChangeContact())
        }
        if (nsi.outcome?.id != outcome.id) {
            nsi.outcome = outcome
            contactRepository.save(nsi.terminationContact())
        }
        if (nsi.actualEndDate != termination.endDate) {
            nsi.actualEndDate = termination.endDate
        }

        createNotificationIfNotExists(termination, nsi)
    }

    private fun findNsi(termination: NsiTermination): Nsi {
        val nsi: Nsi? = nsiRepository.findByPersonCrnAndExternalReference(termination.crn, termination.urn)
        if (nsi == null) {
            val nfr = nsiRepository.getNotFoundReason(termination.crn, termination.urn)
            throw ReferralNotFoundException(
                termination.urn,
                termination.crn,
                termination.eventId,
                termination.startDate.toLocalDate(),
                when {
                    nfr == null -> "NSI cannot be determined"
                    nfr.nsiSoftDeleted == 1 -> "NSI soft deleted"
                    else -> "Unknown"
                }
            )
        }
        return nsi
    }

    private fun Nsi.statusHistory() = NsiStatusHistory(id, status.id, statusDate, notes)
    private fun Nsi.statusChangeContact() = Contact(
        person,
        contactTypeRepository.getReferenceById(status.contactTypeId),
        eventId = eventId,
        nsiId = id,
        date = statusDate.toLocalDate(),
        startTime = statusDate,
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
        nsiId = id,
        date = date.toLocalDate(),
        startTime = date
    )

    private fun Nsi.terminationContact() = Contact(
        person,
        contactTypeRepository.getByCode(NSI_TERMINATED.value),
        providerId = manager.providerId,
        teamId = manager.teamId,
        staffId = manager.staffId,
        eventId = eventId,
        nsiId = id,
        date = statusDate.toLocalDate(),
        startTime = statusDate
    ).addNotes("NSI Terminated with Outcome: ${outcome!!.description}")

    private fun createNotificationIfNotExists(nsiTermination: NsiTermination, nsi: Nsi) {
        nsiTermination.notificationDateTime?.let {
            contactRepository.findNotificationContact(
                nsi.id,
                ContactType.Code.CRSNOTE.value,
                it.toLocalDate()
            ).firstOrNull { c -> c.notes?.contains("End of Service Report Submitted") == true } ?: run {
                contactRepository.save(
                    nsi.contact(ContactType.Code.CRSNOTE.value, it).addNotes(nsiTermination.notificationNotes)
                )
            }
        }
    }
}

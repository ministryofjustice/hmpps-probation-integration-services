package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByEventId
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionTransfer
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionTransferRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getByCodeAndSetName
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getTransferStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.TransferStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.transfer.entity.RejectedTransferDiary
import uk.gov.justice.digital.hmpps.integrations.delius.transfer.entity.RejectedTransferDiaryRepository
import java.time.ZonedDateTime

val EOTL_TERMINATE_LICENCE_CONTACT_NOTES = """${System.lineSeparator()}
    |The date of the termination of licence condition has been identified from the case being updated following a Temporary Absence Return in NOMIS.
    |The date may reflect an update after the date the actual Recall/Return to Custody occurred.
""".trimMargin()

@Service
class LicenceConditionService(
    private val licenceConditionRepository: LicenceConditionRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val licenceConditionTransferRepository: LicenceConditionTransferRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val rejectedTransferDiaryRepository: RejectedTransferDiaryRepository
) {
    fun terminateLicenceConditionsForDisposal(
        disposalId: Long,
        terminationReason: ReferenceData,
        terminationDate: ZonedDateTime,
        endOfTemporaryLicence: Boolean = false
    ) {
        licenceConditionRepository
            .findAllByDisposalIdAndMainCategoryCodeNotAndTerminationReasonIsNull(disposalId)
            .forEach {
                terminateLicenceCondition(it, terminationReason, terminationDate, endOfTemporaryLicence)
            }
    }

    fun terminateLicenceCondition(
        licenceCondition: LicenceCondition,
        terminationReason: ReferenceData,
        terminationDate: ZonedDateTime,
        endOfTemporaryLicence: Boolean = false
    ) {
        // terminate the licence condition
        licenceCondition.terminationDate = terminationDate
        licenceCondition.terminationReason = terminationReason
        licenceCondition.pendingTransfer = false
        licenceConditionRepository.save(licenceCondition)

        // terminate any pending transfers
        terminatePendingTransfers(licenceCondition, terminationDate)

        // delete any future-dated contacts
        contactRepository.deleteAllByLicenceConditionIdAndDateAfterAndOutcomeIdIsNull(
            licenceCondition.id,
            terminationDate
        )

        // create "component terminated" contact
        val event = licenceCondition.disposal.event
        val manager = licenceCondition.manager ?: orderManagerRepository.getByEventId(event.id)
        val notes = "Termination reason: ${terminationReason.description}" +
            if (endOfTemporaryLicence) EOTL_TERMINATE_LICENCE_CONTACT_NOTES else ""
        contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(ContactType.Code.COMPONENT_TERMINATED.value),
                date = terminationDate,
                event = event,
                person = event.person,
                licenceConditionId = licenceCondition.id,
                notes = notes,
                staffId = manager.staffId,
                teamId = manager.teamId
            )
        )
    }

    private fun terminatePendingTransfers(licenceCondition: LicenceCondition, terminationDate: ZonedDateTime) {
        val pendingTransfers = licenceConditionTransferRepository.findAllByLicenceConditionIdAndStatusCode(
            licenceCondition.id,
            TransferStatusCode.PENDING.code
        )
        if (pendingTransfers.isNotEmpty()) {
            val rejectedStatus = referenceDataRepository.getTransferStatus(TransferStatusCode.REJECTED.code)
            val rejectedDecision = referenceDataRepository.getByCodeAndSetName("R", "ACCEPTED DECISION")
            val rejectionReason =
                referenceDataRepository.getByCodeAndSetName("TWR", "LICENCE AREA TRANSFER REJECTION REASON")

            pendingTransfers.forEach { transfer ->
                // reject the transfer
                transfer.status = rejectedStatus
                transfer.decision = rejectedDecision
                transfer.rejectionReason = rejectionReason
                transfer.statusDate = terminationDate
                transfer.notes = listOfNotNull(
                    transfer.notes,
                    "Transfer automatically rejected due to termination of licence condition."
                ).joinToString("\n")

                // created an entry in the 'rejected transfer diary'
                updateRejectedTransferDiary(transfer)

                // create a 'transfer rejected' contact
                createRejectedTransferContact(transfer, terminationDate)
            }
            licenceConditionTransferRepository.saveAll(pendingTransfers)
        }
    }

    private fun createRejectedTransferContact(
        transfer: LicenceConditionTransfer,
        terminationDate: ZonedDateTime
    ) {
        contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(ContactType.Code.COMPONENT_PROVIDER_TRANSFER_REJECTED.value),
                date = terminationDate,
                startTime = terminationDate,
                event = transfer.licenceCondition.disposal.event,
                person = transfer.licenceCondition.disposal.event.person,
                licenceConditionId = transfer.licenceCondition.id,
                staffId = transfer.receivingStaff.id,
                teamId = transfer.receivingTeam.id,
                notes = """
                Transfer Status: ${transfer.status.description}
                Transfer Reason: ${transfer.reason?.description}
                Rejection Reason: ${transfer.rejectionReason?.description}
                Owning Provider: ${transfer.originTeam.probationArea.description}
                Receiving Provider: ${transfer.receivingTeam.probationArea.description}
                Notes: \n${transfer.notes}\n\n
                """.trimIndent()
            )
        )
    }

    private fun updateRejectedTransferDiary(transfer: LicenceConditionTransfer) {
        rejectedTransferDiaryRepository.save(
            RejectedTransferDiary(
                personId = transfer.licenceCondition.disposal.event.person.id,
                eventId = transfer.licenceCondition.disposal.event.id,
                licenceConditionId = transfer.licenceCondition.id,
                licenceConditionTransferId = transfer.id,
                masterTransferId = transfer.masterTransferId,
                requestDate = transfer.requestDate,
                statusDate = transfer.statusDate,
                targetProviderId = transfer.receivingTeam.probationArea.id,
                targetTeamId = transfer.receivingTeam.id,
                targetStaffId = transfer.receivingStaff.id,
                originProviderId = transfer.originTeam.probationArea.id,
                originTeamId = transfer.originTeam.id,
                originStaffId = transfer.originStaff.id,
                rejectionReasonId = transfer.rejectionReason!!.id
            )
        )
    }
}

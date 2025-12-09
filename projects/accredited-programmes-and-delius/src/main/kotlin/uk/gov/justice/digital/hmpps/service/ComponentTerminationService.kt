package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.toDeliusDate
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.contact.ContactType.Companion.COMPONENT_TERMINATED
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.component.SentenceComponent
import uk.gov.justice.digital.hmpps.entity.sentence.component.transfer.RejectedTransferDiary
import uk.gov.justice.digital.hmpps.entity.sentence.component.transfer.Transfer
import uk.gov.justice.digital.hmpps.repository.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class ComponentTerminationService(
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val rejectedTransferDiaryRepository: RejectedTransferDiaryRepository,
    private val telemetryService: TelemetryService,
    private val domainEventService: DomainEventService,
) {
    fun terminate(component: SentenceComponent, occurredAt: ZonedDateTime) {
        if (listOfNotNull(component.startDate, component.commencementDate).max() > occurredAt) {
            telemetryService.trackEvent(
                "ComponentTerminationRejected",
                component.telemetry(
                    "reason" to "Programme completion occurred earlier than the start date",
                    "occurredAt" to occurredAt.toString()
                )
            )
            return
        }

        if (component.terminationDate == null) {
            component
                .apply {
                    terminationDate = occurredAt
                    terminationReason = referenceDataRepository.getByCode(component.completedReason)
                    pendingTransfer = false
                }
                .deleteFutureContacts(occurredAt)
                .createTerminationContact(occurredAt)
                .terminatePendingTransfers(occurredAt)
                .publishTerminationDomainEvent()
            telemetryService.trackEvent("ComponentTerminated", component.telemetry())
        } else {
            component
                .apply { terminationDate = occurredAt }
                .updateTerminationContactDate(occurredAt)
            telemetryService.trackEvent("ComponentTerminationUpdated", component.telemetry())
        }
    }

    private fun SentenceComponent.deleteFutureContacts(date: ZonedDateTime) = also {
        when (this) {
            is Requirement -> contactRepository.deleteFutureRequirementContacts(id, date.toLocalDate())
            is LicenceCondition -> contactRepository.deleteFutureLicenceConditionContacts(id, date.toLocalDate())
        }
    }

    private fun SentenceComponent.terminatePendingTransfers(terminationDate: ZonedDateTime) = also {
        if (pendingTransfers.isNotEmpty()) {
            val rejectedStatus = referenceDataRepository.getByCode(ReferenceData.REJECTED_STATUS)
            val rejectedDecision = referenceDataRepository.getByCode(ReferenceData.REJECTED_DECISION)
            val terminatedReason = referenceDataRepository.getByCode(this.transferRejectionReason)
            pendingTransfers.forEach { transfer ->
                transfer
                    .apply {
                        status = rejectedStatus
                        decision = rejectedDecision
                        rejectionReason = terminatedReason
                        statusDate = terminationDate
                        notes = listOfNotNull(
                            notes,
                            "Transfer automatically rejected due to termination of ${type.lowercase()}."
                        ).joinToString("\n")
                    }
                    .updateRejectedTransferDiary()
                    .createRejectedTransferContact(terminationDate)
            }
        }
    }

    private fun Transfer.updateRejectedTransferDiary() = also {
        rejectedTransferDiaryRepository.save(
            RejectedTransferDiary(
                transfer = this,
                component = component,
                personId = component.disposal.event.person.id,
                eventId = component.disposal.event.id,
                masterTransferId = masterTransferId,
                requestDate = requestDate,
                statusDate = statusDate,
                targetProviderId = receivingTeam.provider.id,
                targetTeamId = receivingTeam.id,
                targetStaffId = receivingStaff.id,
                originProviderId = originTeam.provider.id,
                originTeamId = originTeam.id,
                originStaffId = originStaff.id,
                rejectionReasonId = rejectionReason!!.id
            )
        )
    }

    private fun Transfer.createRejectedTransferContact(terminationDate: ZonedDateTime) {
        contactRepository.save(
            Contact(
                person = component.disposal.event.person.asPersonCrn(),
                event = component.disposal.event,
                component = component,
                date = terminationDate.toLocalDate(),
                startTime = terminationDate,
                type = contactTypeRepository.getByCode(ContactType.COMPONENT_TRANSFER_REJECTED),
                staff = receivingStaff,
                team = receivingTeam,
                provider = receivingTeam.provider,
                notes = """
                    Transfer Status: ${status.description}
                    Transfer Reason: ${allocationReason?.description}
                    Rejection Reason: ${rejectionReason?.description}
                    Owning Provider: ${originTeam.provider.description}
                    Receiving Provider: ${receivingTeam.provider.description}
                    Notes: 
                    $notes
                    
                """.trimIndent()
            )
        )
    }

    private fun SentenceComponent.createTerminationContact(occurredAt: ZonedDateTime) = also {
        val manager = checkNotNull(manager) { "$type with id=$id has no manager" }
        contactRepository.save(
            Contact(
                component = this,
                person = disposal.event.person.asPersonCrn(),
                event = disposal.event,
                date = occurredAt.toLocalDate(),
                startTime = occurredAt,
                type = contactTypeRepository.getByCode(COMPONENT_TERMINATED),
                staff = manager.staff,
                team = manager.team,
                provider = manager.team.provider,
                notes = "$type terminated on ${occurredAt.toDeliusDate()}${terminationReason?.let { " with termination reason of \"${it.description}\"" } ?: ""} following notification from the Accredited Programmes – Intervention Service",
            )
        )
    }

    private fun SentenceComponent.updateTerminationContactDate(occurredAt: ZonedDateTime) {
        val contact = when (this) {
            is Requirement -> contactRepository.findByRequirementIdAndTypeCode(id, COMPONENT_TERMINATED)
            is LicenceCondition -> contactRepository.findByLicenceConditionIdAndTypeCode(id, COMPONENT_TERMINATED)
            else -> null
        }
        contact?.apply {
            date = occurredAt.toLocalDate()
            startTime = occurredAt
        } ?: createTerminationContact(occurredAt)
    }

    private fun SentenceComponent.publishTerminationDomainEvent() {
        when (this) {
            is Requirement -> domainEventService.publishTermination(this)
            is LicenceCondition -> domainEventService.publishTermination(this)
        }
    }

    private fun SentenceComponent.telemetry(vararg pairs: Pair<String, String>) = pairs.toMap() + mapOf(
        "type" to this::class.simpleName!!,
        "id" to id.toString(),
        "crn" to disposal.event.person.crn,
        "startDate" to startDate.toString(),
        "commencementDate" to commencementDate?.toString(),
        "terminationDate" to terminationDate?.toString(),
    )
}

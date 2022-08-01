package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactContext
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.findByCodeOrThrow
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.RequirementAllocationDetail

@Service
class AllocateRequirementService(
    private val auditedInteractionService: AuditedInteractionService,
    private val requirementRepository: RequirementRepository,
    private val requirementManagerRepository: RequirementManagerRepository,
    private val allocationValidator: AllocationValidator,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val transferReasonRepository: TransferReasonRepository
) : ManagerService<RequirementManager>(requirementManagerRepository) {

    @Transactional
    fun createRequirementAllocation(crn: String, allocationDetail: RequirementAllocationDetail) {
        val requirement = requirementRepository.findByIdOrNull(allocationDetail.requirementId)
            ?: throw NotFoundException("Requirement", "id", allocationDetail.requirementId)

        if (requirement.person.crn != crn)
            throw ConflictException("Requirement ${allocationDetail.requirementId} not for $crn")
        if (requirement.disposal.event.id != allocationDetail.eventId)
            throw ConflictException("Requirement ${allocationDetail.requirementId} not for event ${allocationDetail.eventId}")
        if (!requirement.active) throw NotActiveException("Requirement", "id", allocationDetail.requirementId)

        auditedInteractionService.createAuditedInteraction(
            BusinessInteractionCode.CREATE_COMPONENT_TRANSFER,
            AuditedInteraction.Parameters(
                "offenderId" to requirement.person.id.toString(),
                "eventId" to requirement.disposal.event.id.toString(),
                "requirementId" to requirement.id.toString()
            )
        )

        val activeRequirementManager = requirementManagerRepository.findActiveManagerAtDate(
            allocationDetail.requirementId, allocationDetail.createdDate
        ) ?: throw NotFoundException(
            "Requirement Manager for requirement ${allocationDetail.requirementId} at ${allocationDetail.createdDate} not found"
        )

        if (allocationDetail.isDuplicate(activeRequirementManager)) {
            return
        }

        if (requirementRepository.countPendingTransfers(requirement.id) > 0) {
            throw ConflictException("Pending transfer exists for this requirement: ${requirement.id}")
        }
        val ts = allocationValidator.initialValidations(
            activeRequirementManager.provider.id,
            allocationDetail,
        )

        val transferReason = transferReasonRepository.findByCode(TransferReasonCode.COMPONENT.value)
            ?: throw NotFoundException("Transfer Reason", "code", TransferReasonCode.COMPONENT.value)

        val newRequirementManager = RequirementManager(
            requirementId = allocationDetail.requirementId,
            transferReasonId = transferReason.id
        ).apply {
            populate(allocationDetail.createdDate, ts, activeRequirementManager)
        }

        val (activeOM, newOM) = updateDateTimes(activeRequirementManager, newRequirementManager)

        contactRepository.save(
            createTransferContact(
                activeOM,
                newOM,
                ContactContext(
                    contactTypeRepository.findByCodeOrThrow(ContactTypeCode.SENTENCE_COMPONENT_TRANSFER.value),
                    requirement.person.id,
                    allocationDetail.eventId,
                    allocationDetail.requirementId
                )
            )
        )

        requirementRepository.updateIaps(allocationDetail.requirementId)
    }
}

package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exceptions.ConflictException
import uk.gov.justice.digital.hmpps.exceptions.RequirementManagerNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.RequirementNotActiveException
import uk.gov.justice.digital.hmpps.exceptions.RequirementNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.TransferReasonNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.audit.service.AuditedInteractionService
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
    private val allocationRequestValidator: AllocationRequestValidator,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val transferReasonRepository: TransferReasonRepository
) : ManagerService<RequirementManager>(requirementManagerRepository) {

    @Transactional
    fun createRequirementAllocation(crn: String, allocationDetail: RequirementAllocationDetail) {
        val requirement = requirementRepository.findByIdOrNull(allocationDetail.requirementId)
            ?: throw RequirementNotFoundException(allocationDetail.requirementId)

        if (requirement.person.crn != crn) throw ConflictException("Requirement ${allocationDetail.requirementId} not for $crn")
        if (requirement.disposal.event.id != allocationDetail.eventId) throw ConflictException("Requirement ${allocationDetail.requirementId} not for event ${allocationDetail.eventId}")
        if (!requirement.active) throw RequirementNotActiveException(allocationDetail.requirementId)

        auditedInteractionService.createAuditedInteraction(
            BusinessInteractionCode.ADD_EVENT_ALLOCATION,
            AuditedInteraction.Parameters(
                "offenderId" to requirement.person.id.toString(),
                "eventId" to allocationDetail.eventId.toString(),
                "requirementId" to requirement.id.toString()
            )
        )

        val activeRequirementManager = requirementManagerRepository.findActiveManagerAtDate(
            allocationDetail.requirementId, allocationDetail.createdDate
        ) ?: throw RequirementManagerNotFoundException(allocationDetail.requirementId, allocationDetail.createdDate)

        if (allocationDetail.isDuplicate(activeRequirementManager)) {
            return
        }

        allocationRequestValidator.hasNoPendingTransfers(requirement)
        val ts = allocationRequestValidator.initialValidations(
            activeRequirementManager.provider.id,
            allocationDetail,
        )

        val transferReason = transferReasonRepository.findByCode(TransferReasonCode.COMPONENT.value)
            ?: throw TransferReasonNotFoundException(TransferReasonCode.COMPONENT.value)

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

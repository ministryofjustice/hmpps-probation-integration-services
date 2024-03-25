package uk.gov.justice.digital.hmpps.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationValidator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.RequirementAllocation

@Service
class AllocateRequirementService(
    auditedInteractionService: AuditedInteractionService,
    private val requirementRepository: RequirementRepository,
    private val requirementManagerRepository: RequirementManagerRepository,
    private val allocationValidator: AllocationValidator,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val optimisationTables: OptimisationTables
) : ManagerService<RequirementManager>(auditedInteractionService, requirementManagerRepository) {

    @Transactional
    fun createRequirementAllocation(crn: String, allocationDetail: RequirementAllocation) =
        audit(BusinessInteractionCode.CREATE_COMPONENT_TRANSFER) { audit ->
            val requirement = requirementRepository.findByIdOrNull(allocationDetail.requirementId)
                ?: throw NotFoundException("Requirement", "id", allocationDetail.requirementId)

            audit["offenderId"] = requirement.person.id
            audit["eventId"] = requirement.disposal.event.id
            audit["requirementId"] = requirement.id
            optimisationTables.rebuild(requirement.person.id)

            if (requirement.person.crn != crn) {
                throw ConflictException("Requirement ${allocationDetail.requirementId} not for $crn")
            }
            if (requirement.disposal.event.number != allocationDetail.eventNumber.toString()) {
                throw ConflictException("Requirement ${allocationDetail.requirementId} not for event ${allocationDetail.eventNumber}")
            }
            if (!requirement.disposal.active || !requirement.disposal.event.active) {
                throw NotActiveException("Event", "number", requirement.disposal.event.number)
            }
            if (!requirement.active) throw NotActiveException("Requirement", "id", allocationDetail.requirementId)

            val activeRequirementManager = requirementManagerRepository.findActiveManagerAtDate(
                allocationDetail.requirementId,
                allocationDetail.createdDate
            ) ?: throw NotFoundException(
                "Requirement Manager for requirement ${allocationDetail.requirementId} at ${allocationDetail.createdDate} not found"
            )

            if (allocationDetail.isDuplicate(activeRequirementManager)) {
                return@audit
            }

            if (requirementRepository.countPendingTransfers(requirement.id) > 0) {
                throw IgnorableMessageException(
                    "Pending transfer exists in Delius",
                    listOfNotNull(
                        "eventNumber" to requirement.disposal.event.number,
                        requirement.mainCategory?.description?.let { "mainCategory" to it },
                        requirement.subCategory?.description?.let { "subCategory" to it }
                    ).toMap()
                )
            }
            val ts = allocationValidator.initialValidations(
                activeRequirementManager.provider.id,
                allocationDetail
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
                        requirement.disposal.event.id,
                        requirement.id
                    )
                )
            )

            if (requirement.isAccreditedProgramme()) {
                requirementRepository.updateIaps(allocationDetail.requirementId)
            }
        }
}

private const val ACCREDITED_PROGRAMME = "7"
private const val WY_ACCRED_PROGRAMME = "RM38"
private const val NOT_SPECIFIED = "RS66"
private fun Requirement.isAccreditedProgramme() = when {
    mainCategory?.code == ACCREDITED_PROGRAMME && subCategory?.code != NOT_SPECIFIED -> true
    mainCategory?.code == WY_ACCRED_PROGRAMME -> true
    additionalMainCategory?.code in arrayOf(ACCREDITED_PROGRAMME, WY_ACCRED_PROGRAMME) -> true
    else -> false
}

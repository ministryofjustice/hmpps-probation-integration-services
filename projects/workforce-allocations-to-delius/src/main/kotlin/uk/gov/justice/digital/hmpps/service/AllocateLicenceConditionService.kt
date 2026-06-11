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
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.LicenceConditionAllocation

@Service
class AllocateLicenceConditionService(
    auditedInteractionService: AuditedInteractionService,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val licenceConditionManagerRepository: LicenceConditionManagerRepository,
    private val allocationValidator: AllocationValidator,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val optimisationTables: OptimisationTables
) : ManagerService<LicenceConditionManager>(auditedInteractionService, licenceConditionManagerRepository) {

    @Transactional
    fun createLicenceConditionAllocation(crn: String, allocationDetail: LicenceConditionAllocation) =
        audit(BusinessInteractionCode.CREATE_COMPONENT_TRANSFER) { audit ->
            val licenceCondition = licenceConditionRepository.findByIdOrNull(allocationDetail.licenceConditionId)
                ?: throw IgnorableMessageException(
                    "Requirement no longer exists",
                    mapOf("id" to allocationDetail.licenceConditionId.toString())
                )

            audit["offenderId"] = licenceCondition.person.id
            audit["eventId"] = licenceCondition.disposal.event.id
            audit["requirementId"] = licenceCondition.id
            optimisationTables.rebuild(licenceCondition.person.id)

            if (licenceCondition.person.crn != crn) {
                throw ConflictException("Licence Condition ${allocationDetail.licenceConditionId} not for $crn")
            }
            if (licenceCondition.disposal.event.number != allocationDetail.eventNumber.toString()) {
                throw ConflictException("Licence Condition ${allocationDetail.licenceConditionId} not for event ${allocationDetail.eventNumber}")
            }
            if (!licenceCondition.disposal.active || !licenceCondition.disposal.event.active) {
                throw NotActiveException("Event", "number", licenceCondition.disposal.event.number)
            }
            if (!licenceCondition.active) throw NotActiveException(
                "Licence Condition",
                "id",
                allocationDetail.licenceConditionId
            )

            val activeLicenceConditionManager = licenceConditionManagerRepository.findActiveManagerAtDate(
                allocationDetail.licenceConditionId,
                allocationDetail.createdDate
            ) ?: throw NotFoundException(
                "Licence Condition Manager for licence condition ${allocationDetail.licenceConditionId} at ${allocationDetail.createdDate} not found"
            )

            if (allocationDetail.isDuplicate(activeLicenceConditionManager)) {
                return@audit
            }

            if (licenceConditionRepository.countPendingTransfers(licenceCondition.id) > 0) {
                throw IgnorableMessageException(
                    "Pending transfer exists in Delius",
                    listOfNotNull(
                        "eventNumber" to licenceCondition.disposal.event.number,
                        "mainCategory" to licenceCondition.mainCategory.description,
                        licenceCondition.subCategory?.description?.let { "subCategory" to it }
                    ).toMap()
                )
            }
            val ts = allocationValidator.initialValidations(
                activeLicenceConditionManager.provider.id,
                allocationDetail
            )

            val transferReason = transferReasonRepository.findByCode(TransferReasonCode.COMPONENT.value)
                ?: throw NotFoundException("Transfer Reason", "code", TransferReasonCode.COMPONENT.value)

            val newLicenceConditionManager = LicenceConditionManager(
                licenceConditionId = allocationDetail.licenceConditionId,
                transferReasonId = transferReason.id
            ).apply {
                populate(allocationDetail.createdDate, ts, activeLicenceConditionManager)
            }

            val (activeOM, newOM) = updateDateTimes(activeLicenceConditionManager, newLicenceConditionManager)

            contactRepository.save(
                createTransferContact(
                    activeOM,
                    newOM,
                    ContactContext(
                        contactTypeRepository.findByCodeOrThrow(ContactTypeCode.SENTENCE_COMPONENT_TRANSFER.value),
                        offenderId = licenceCondition.person.id,
                        eventId = licenceCondition.disposal.event.id,
                        licenceConditionId = licenceCondition.id
                    )
                )
            )
        }
}


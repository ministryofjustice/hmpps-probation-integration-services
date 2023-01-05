package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datasource.OptimisationContext
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationValidator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactContext
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.findByCodeOrThrow
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode.CASE_ORDER
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.getByPersonCrnAndNumber
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.EventAllocationDetail

@Service
class AllocateEventService(
    auditedInteractionService: AuditedInteractionService,
    private val eventRepository: EventRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val allocationValidator: AllocationValidator,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val transferReasonRepository: TransferReasonRepository
) : ManagerService<OrderManager>(auditedInteractionService, orderManagerRepository) {

    @Transactional
    fun createEventAllocation(crn: String, allocationDetail: EventAllocationDetail) =
        audit(BusinessInteractionCode.ADD_EVENT_ALLOCATION) {
            val event = eventRepository.getByPersonCrnAndNumber(crn, allocationDetail.eventNumber.toString())

            it["offenderId"] = event.person.id
            it["eventId"] = event.id
            OptimisationContext.offenderId.set(event.person.id)

            if (!event.active) throw NotActiveException("Event", "number", allocationDetail.eventNumber)

            val activeOrderManager = orderManagerRepository.findActiveManagerAtDate(
                event.id, allocationDetail.createdDate
            ) ?: throw NotFoundException(
                "Order Manager for event ${allocationDetail.eventNumber} at ${allocationDetail.createdDate} not found"
            )

            if (allocationDetail.isDuplicate(activeOrderManager)) {
                return@audit
            }

            if (eventRepository.countPendingTransfers(event.id) > 0) {
                throw ConflictException("Pending transfer exists for this event: ${event.id}")
            }
            val ts = allocationValidator.initialValidations(
                activeOrderManager.provider.id,
                allocationDetail,
            )

            val transferReason = transferReasonRepository.findByCode(CASE_ORDER.value)
                ?: throw NotFoundException("Transfer Reason", "code", CASE_ORDER.value)

            val newOrderManager = OrderManager(eventId = event.id, transferReasonId = transferReason.id).apply {
                populate(allocationDetail.createdDate, ts, activeOrderManager)
            }

            val (activeOM, newOM) = updateDateTimes(activeOrderManager, newOrderManager)

            contactRepository.save(
                createTransferContact(
                    activeOM,
                    newOM,
                    ContactContext(
                        contactTypeRepository.findByCodeOrThrow(ContactTypeCode.ORDER_SUPERVISOR_TRANSFER.value),
                        event.person.id,
                        event.id
                    )
                )
            )

            eventRepository.updateIaps(event.id)
        }
}

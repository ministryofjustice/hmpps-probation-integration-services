package uk.gov.justice.digital.hmpps.integrations.delius.allocations.person

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exceptions.ConflictException
import uk.gov.justice.digital.hmpps.exceptions.EventNotActiveException
import uk.gov.justice.digital.hmpps.exceptions.EventNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.OrderManagerNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.TransferReasonNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationRequestValidator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ManagerService
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
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.EventAllocationDetail

@Service
class AllocateEventService(
    private val eventRepository: EventRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val allocationRequestValidator: AllocationRequestValidator,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val transferReasonRepository: TransferReasonRepository
) : ManagerService<OrderManager>(orderManagerRepository) {

    @Transactional
    fun createEventAllocation(crn: String, allocationDetail: EventAllocationDetail) {
        val event = eventRepository.findByIdOrNull(allocationDetail.eventId)
            ?: throw EventNotFoundException(allocationDetail.eventId)

        if (!event.active) throw EventNotActiveException(allocationDetail.eventId)
        if (event.person.crn != crn) throw ConflictException("Event ${allocationDetail.eventId} not for $crn")

        // TODO: Auditing

        val activeOrderManager =
            orderManagerRepository.findActiveManagerAtDate(allocationDetail.eventId, allocationDetail.createdDate)
                ?: throw OrderManagerNotFoundException(allocationDetail.eventId, allocationDetail.createdDate)

        if (allocationDetail.isDuplicate(activeOrderManager)) {
            return
        }

        allocationRequestValidator.hasNoPendingTransfers(event)
        val ts = allocationRequestValidator.initialValidations(
            activeOrderManager.provider.id,
            allocationDetail,
        )

        val transferReason = transferReasonRepository.findByCode(CASE_ORDER.value)
            ?: throw TransferReasonNotFoundException(CASE_ORDER.value)

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

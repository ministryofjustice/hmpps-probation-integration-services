package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationValidator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode.CASE_ORDER
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.EventAllocation

@Service
class AllocateEventService(
    auditedInteractionService: AuditedInteractionService,
    private val eventRepository: EventRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val allocationValidator: AllocationValidator,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val optimisationTables: OptimisationTables,
    private val staffRepository: StaffRepository,
    private val featureFlags: FeatureFlags
) : ManagerService<OrderManager>(auditedInteractionService, orderManagerRepository) {

    @Transactional
    fun createEventAllocation(crn: String, allocationDetail: EventAllocation) =
        audit(BusinessInteractionCode.ADD_EVENT_ALLOCATION) {
            val event = eventRepository.getByPersonCrnAndNumber(crn, allocationDetail.eventNumber.toString())

            it["offenderId"] = event.person.id
            it["eventId"] = event.id
            optimisationTables.rebuild(event.person.id)

            if (!event.active) throw NotActiveException("Event", "number", allocationDetail.eventNumber)

            val activeOrderManager = orderManagerRepository.findActiveManagerAtDate(
                event.id,
                allocationDetail.createdDate
            ) ?: throw NotFoundException(
                "Order Manager for event ${allocationDetail.eventNumber} at ${allocationDetail.createdDate} not found"
            )

            if (allocationDetail.isDuplicate(activeOrderManager)) {
                return@audit
            }

            if (eventRepository.countPendingTransfers(event.id) > 0) {
                throw IgnorableMessageException(
                    "Pending transfer exists in Delius",
                    mapOf("eventNumber" to event.number)
                )
            }
            val ts = allocationValidator.initialValidations(
                activeOrderManager.provider.id,
                allocationDetail
            )

            val transferReason = transferReasonRepository.findByCode(CASE_ORDER.value)
                ?: throw NotFoundException("Transfer Reason", "code", CASE_ORDER.value)

            val spoStaff = if (allocationDetail.spoStaffCode != null)
                staffRepository.findByCode(allocationDetail.spoStaffCode)
                    ?: throw NotFoundException("SPO Staff", "code", allocationDetail.spoStaffCode) else null

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

            if (featureFlags.enabled("produce-spod03-contact")) {
                createSPOContact(allocationDetail, event, newOrderManager, spoStaff)
            } else {
                createCadeContact(allocationDetail, event, newOrderManager, spoStaff)
            }

            if (event.hasAccreditedProgrammeRequirement()) {
                eventRepository.updateIaps(event.id)
            }
        }

    fun createCadeContact(
        allocationDetail: EventAllocation,
        event: Event,
        orderManager: OrderManager,
        spoStaff: Staff?
    ) {
        contactRepository.save(
            Contact(
                type = contactTypeRepository.findByCodeOrThrow(ContactTypeCode.CASE_ALLOCATION_DECISION_EVIDENCE.value),
                personId = event.person.id,
                eventId = event.id,
                date = orderManager.startDate.toLocalDate(),
                startTime = orderManager.startDate,
                teamId = orderManager.team.id,
                staffId = spoStaff?.id ?: orderManager.staff.id,
                providerId = orderManager.provider.id,
                notes = allocationDetail.notes,
                isSensitive = allocationDetail.sensitive ?: false
            )
        )
    }

    fun createSPOContact(
        allocationDetail: EventAllocation,
        event: Event,
        orderManager: OrderManager,
        spoStaff: Staff?
    ) {
        contactRepository.save(
            Contact(
                type = contactTypeRepository.findByCodeOrThrow(ContactTypeCode.CASE_ALLOCATION_SPO_OVERSIGHT.value),
                personId = event.person.id,
                eventId = event.id,
                date = orderManager.startDate.toLocalDate(),
                startTime = orderManager.startDate,
                teamId = orderManager.team.id,
                staffId = spoStaff?.id ?: orderManager.staff.id,
                providerId = orderManager.provider.id,
                notes = allocationDetail.spoOversightNotes,
                isSensitive = allocationDetail.sensitiveOversightNotes ?: true
            )
        )
    }

    fun Event.hasAccreditedProgrammeRequirement(): Boolean =
        eventRepository.countAccreditedProgrammeRequirements(id) > 0
}

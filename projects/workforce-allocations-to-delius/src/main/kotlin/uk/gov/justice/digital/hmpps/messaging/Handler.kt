package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocateEventService
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocatePersonService
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocateRequirementService
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.EventAllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.PersonAllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.RequirementAllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.WorkforceAllocationsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
class Handler(
    private val allocationsClient: WorkforceAllocationsClient,
    private val allocatePersonService: AllocatePersonService,
    private val allocateEventService: AllocateEventService,
    private val allocateRequirementService: AllocateRequirementService,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val allocationEvent = notification.message

        when (val allocationDetail = allocationsClient.getAllocationDetail(URI.create(allocationEvent.detailUrl!!))) {
            is PersonAllocationDetail -> allocatePersonService.createPersonAllocation(allocationDetail)
            is EventAllocationDetail -> allocateEventService.createEventAllocation(
                allocationEvent.findCrn(), allocationDetail
            )
            is RequirementAllocationDetail -> allocateRequirementService.createRequirementAllocation(
                allocationEvent.findCrn(), allocationDetail
            )
        }
    }

    override fun getMessageType() = HmppsDomainEvent::class

    fun HmppsDomainEvent.findCrn(): String =
        personReference.findCrn() ?: throw IllegalArgumentException("No CRN available in person reference")
}

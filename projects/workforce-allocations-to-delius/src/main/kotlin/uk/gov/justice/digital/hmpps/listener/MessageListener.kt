package uk.gov.justice.digital.hmpps.listener

import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
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
@EnableJms
class MessageListener(
    private val allocationsClient: WorkforceAllocationsClient,
    private val allocatePersonService: AllocatePersonService,
    private val allocateEventService: AllocateEventService,
    private val allocateRequirementService: AllocateRequirementService,
    private val telemetryService: TelemetryService
) {
    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(notification: Notification<HmppsDomainEvent>) {
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

    fun HmppsDomainEvent.findCrn(): String =
        personReference.findCrn() ?: throw IllegalArgumentException("No CRN available in person reference")
}

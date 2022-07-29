package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.person.AllocatePersonService
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.EventAllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.PersonAllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.RequirementAllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationEvent
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.WorkforceAllocationsClient
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Component
@EnableJms
class MessageListener(
    val allocationsClient: WorkforceAllocationsClient,
    val allocatePersonService: AllocatePersonService,
    val telemetryService: TelemetryService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(allocationEvent: AllocationEvent) {
        log.info("received $allocationEvent")
        telemetryService.trackEvent(
            "${allocationEvent.eventType}_RECEIVED",
            mapOf(
                "eventType" to allocationEvent.eventType.value,
                "detailUrl" to allocationEvent.detailUrl
            ) + allocationEvent.personReference.identifiers.associate { Pair(it.type, it.value) }
        )

        when (val allocationDetail = allocationsClient.getAllocationDetail(URI.create(allocationEvent.detailUrl))) {
            is PersonAllocationDetail -> allocatePersonService.createPersonAllocation(allocationDetail)
            is EventAllocationDetail -> {}
            is RequirementAllocationDetail -> {}
        }
    }
}

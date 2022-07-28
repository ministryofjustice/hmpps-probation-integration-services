package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationEvent
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.WorkforceAllocationsClient
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Component
@EnableJms
class MessageListener(
    val allocationsClient: WorkforceAllocationsClient,
    val telemetryService: TelemetryService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(allocationEvent: AllocationEvent) {
        log.info("received $allocationEvent")
        telemetryService.trackEvent(
            "${allocationEvent.eventType}_RECEIVED", mapOf(
                "eventType" to allocationEvent.eventType.value,
                "detailUrl" to allocationEvent.detailUrl
            ) + allocationEvent.personReference.identifiers.associate { Pair(it.type, it.value) }
        )

        when (allocationsClient.getAllocationDetail(URI.create(allocationEvent.detailUrl))) {
            is AllocationDetail.PersonAllocationDetail -> {}
            is AllocationDetail.EventAllocationDetail -> {}
            is AllocationDetail.RequirementAllocationDetail -> {}
        }
    }
}

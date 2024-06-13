package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.*
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.WorkforceAllocationsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.AllocateEventService
import uk.gov.justice.digital.hmpps.service.AllocatePersonService
import uk.gov.justice.digital.hmpps.service.AllocateRequirementService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI.create

@Component
@Channel("workforce-allocations-to-delius-queue")
class Handler(
    private val allocationsClient: WorkforceAllocationsClient,
    private val allocatePersonService: AllocatePersonService,
    private val allocateEventService: AllocateEventService,
    private val allocateRequirementService: AllocateRequirementService,
    private val telemetryService: TelemetryService,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(name = "workforce/person_allocation"),
            Message(name = "workforce/event_allocation"),
            Message(name = "workforce/requirement_allocation"),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        val allocationEvent = notification.message
        val detailUrl = checkNotNull(allocationEvent.detailUrl)
        val allocationDetail = allocationsClient.getAllocationDetail(create(detailUrl))
        try {
            when (allocationDetail) {
                is PersonAllocation -> allocatePersonService.createPersonAllocation(allocationDetail)
                is EventAllocation -> allocateEventService.createEventAllocation(
                    allocationEvent.findCrn(),
                    allocationDetail
                )

                is RequirementAllocation -> allocateRequirementService.createRequirementAllocation(
                    allocationEvent.findCrn(),
                    allocationDetail
                )
            }
            telemetryService.trackEvent(
                allocationDetail::class.simpleName!!,
                mapOf("crn" to allocationEvent.findCrn(), "detailUrl" to detailUrl)
            )
        } catch (ex: IgnorableMessageException) {
            telemetryService.trackEvent(
                "AllocationFailed",
                mapOf(
                    "type" to allocationDetail::class.simpleName!!,
                    "crn" to allocationEvent.findCrn(),
                    "reason" to ex.message
                ) + ex.additionalProperties
            )
        }
    }

    fun HmppsDomainEvent.findCrn(): String =
        personReference.findCrn() ?: throw IllegalArgumentException("No CRN available in person reference")
}

package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.*
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.AllocateEventService
import uk.gov.justice.digital.hmpps.service.AllocatePersonService
import uk.gov.justice.digital.hmpps.service.AllocateRequirementService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("workforce-allocations-to-delius-queue")
class Handler(
    private val detailService: DomainEventDetailService,
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
        val allocationDetail: AllocationDetail =
            allocationEvent.detailUrl?.let { detailService.getDetail(allocationEvent) } ?: return
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
                mapOf("crn" to allocationEvent.findCrn(), "detailUrl" to allocationEvent.detailUrl!!)
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

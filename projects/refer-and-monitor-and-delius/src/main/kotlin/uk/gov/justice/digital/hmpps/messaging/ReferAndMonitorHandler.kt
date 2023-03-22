package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Failure
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Success
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
class ReferAndMonitorHandler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    eventHandlers: List<DomainEventHandler>
) : NotificationHandler<HmppsDomainEvent> {
    private val eventHandlers: Map<DomainEventType, (HmppsDomainEvent) -> EventProcessingResult> =
        eventHandlers.flatMap { it.handledEvents.entries }.associate { it.key to it.value }

    override fun handle(notification: Notification<HmppsDomainEvent>) {
        if (notification.message.detailUrl == null) throw IllegalArgumentException("Detail Url Missing")
        val event = DomainEventType.of(notification.eventType ?: notification.message.eventType)
        when (val res = eventHandlers[event]?.invoke(notification.message)) {
            is Success -> telemetryService.trackEvent(
                res.eventType::class.simpleName!!,
                res.properties
            )
            is Failure -> {
                telemetryService.trackEvent(
                    res.exception::class.simpleName!!,
                    res.properties + ("message" to res.exception.message!!)
                )
                throw res.exception
            }
            null -> telemetryService.trackEvent(
                "UnhandledEventReceived",
                mapOf("eventType" to event.name)
            )
        }
    }
}

interface DomainEventHandler {
    val handledEvents: Map<DomainEventType, (HmppsDomainEvent) -> EventProcessingResult>
    fun handle(block: () -> EventProcessingResult): EventProcessingResult = try {
        block()
    } catch (e: Exception) {
        Failure(e, mapOf("message" to (e.message ?: "")))
    }
}

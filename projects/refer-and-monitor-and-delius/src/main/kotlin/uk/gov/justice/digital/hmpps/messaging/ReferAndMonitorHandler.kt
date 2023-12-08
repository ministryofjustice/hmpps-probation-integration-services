package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.UnprocessableException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Failure
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Rejected
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Success
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
class ReferAndMonitorHandler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    eventHandlers: List<DomainEventHandler>,
) : NotificationHandler<HmppsDomainEvent> {
    private val eventHandlers: Map<DomainEventType, (HmppsDomainEvent) -> EventProcessingResult> =
        eventHandlers.flatMap { it.handledEvents.entries }.associate { it.key to it.value }

    override fun handle(notification: Notification<HmppsDomainEvent>) {
        requireNotNull(notification.message.detailUrl) { "Detail Url Missing" }
        val event = DomainEventType.of(notification.eventType ?: notification.message.eventType)
        when (val res = eventHandlers[event]?.invoke(notification.message)) {
            is Success ->
                telemetryService.trackEvent(
                    res.eventType::class.simpleName!!,
                    notification.message.commonFields() + res.properties,
                )
            is Failure -> {
                telemetryService.trackEvent(
                    res.exception::class.simpleName!!,
                    notification.message.commonFields() + res.properties + ("message" to res.exception.message!!),
                )
                throw res.exception
            }
            is Rejected -> {
                telemetryService.trackEvent(
                    res.exception.message!!,
                    notification.message.commonFields() + res.properties,
                )
            }
            null ->
                telemetryService.trackEvent(
                    "UnhandledEventReceived",
                    notification.message.commonFields() + ("eventType" to event.name),
                )
        }
    }
}

fun HmppsDomainEvent.commonFields() =
    mapOf(
        "crn" to (personReference.findCrn() ?: ""),
        "referralId" to (additionalInformation["referralId"] as String? ?: ""),
    )

interface DomainEventHandler {
    val handledEvents: Map<DomainEventType, (HmppsDomainEvent) -> EventProcessingResult>

    fun handle(
        event: HmppsDomainEvent,
        block: (event: HmppsDomainEvent) -> EventProcessingResult,
    ): EventProcessingResult =
        try {
            block(event)
        } catch (upe: UnprocessableException) {
            Rejected(upe, event.commonFields() + upe.properties)
        } catch (e: Exception) {
            Failure(e, event.commonFields() + ("message" to (e.message ?: "")))
        }
}

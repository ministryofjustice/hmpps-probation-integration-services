package uk.gov.justice.digital.hmpps.telemetry

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import org.springframework.messaging.MessageHeaders
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification

object TelemetryMessagingExtensions {
    fun MessageHeaders.withSpanContext(): MessageHeaders {
        val map = this.toMutableMap()
        val context = Context.current().with(Span.current())
        GlobalOpenTelemetry.getPropagators().textMapPropagator
            .inject(context, map) { carrier, key, value -> carrier!![key] = value }
        return MessageHeaders(map)
    }

    fun MessageAttributes.extractSpanContext(): Context {
        val getter = object : TextMapGetter<MessageAttributes> {
            override fun keys(carrier: MessageAttributes) = carrier.keys
            override fun get(carrier: MessageAttributes?, key: String) = carrier?.get(key)?.value
        }
        return GlobalOpenTelemetry.getPropagators().textMapPropagator.extract(Context.current(), this, getter)
    }

    fun Context.startSpan(scopeName: String, spanName: String, spanKind: SpanKind = SpanKind.INTERNAL): Span {
        val tracer = GlobalOpenTelemetry.getTracer(scopeName)
        return tracer.spanBuilder(spanName).setParent(this).setSpanKind(spanKind).startSpan()
    }

    fun TelemetryService.hmppsEventReceived(hmppsEvent: HmppsDomainEvent) {
        trackEvent(
            "NotificationReceived",
            mapOf("eventType" to hmppsEvent.eventType) +
                (hmppsEvent.detailUrl?.let { mapOf("detailUrl" to it) } ?: mapOf()) +
                (hmppsEvent.personReference.identifiers.associate { Pair(it.type, it.value) })
        )
    }

    fun <T> TelemetryService.notificationReceived(notification: Notification<T>) {
        if (notification.message is HmppsDomainEvent) {
            hmppsEventReceived(notification.message)
        } else {
            trackEvent("NotificationReceived", notification.eventType?.let { mapOf("eventType" to it) } ?: mapOf())
        }
    }
}

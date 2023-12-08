package uk.gov.justice.digital.hmpps.telemetry

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification

fun TelemetryService.hmppsEventReceived(hmppsEvent: HmppsDomainEvent) {
    trackEvent(
        "${hmppsEvent.eventType.uppercase().replace(".", "_")}_RECEIVED",
        mapOf("eventType" to hmppsEvent.eventType) +
            (hmppsEvent.detailUrl?.let { mapOf("detailUrl" to it) } ?: mapOf()) +
            (hmppsEvent.personReference.identifiers.associate { Pair(it.type, it.value) }),
    )
}

fun <T> TelemetryService.notificationReceived(notification: Notification<T>) {
    if (notification.message is HmppsDomainEvent) {
        hmppsEventReceived(notification.message)
    } else {
        notification.eventType?.let {
            trackEvent("${it.uppercase().replace(".", "_")}_RECEIVED", mapOf("eventType" to it))
        } ?: trackEvent("UNKNOWN_EVENT_RECEIVED")
    }
}

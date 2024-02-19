package uk.gov.justice.digital.hmpps.publisher

import uk.gov.justice.digital.hmpps.message.Notification

interface NotificationPublisher {
    fun publish(notification: Notification<*>)
}

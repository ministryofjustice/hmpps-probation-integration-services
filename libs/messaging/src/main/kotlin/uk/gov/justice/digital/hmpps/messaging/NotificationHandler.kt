package uk.gov.justice.digital.hmpps.messaging

import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.Notification

interface NotificationHandler<T : Any> {
    val converter: NotificationConverter<T>

    fun handle(notification: Notification<T>)

    fun handle(message: String) {
        handle(converter.fromMessage(message))
    }
}

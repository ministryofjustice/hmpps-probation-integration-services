package uk.gov.justice.digital.hmpps.listener

import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler

abstract class NotificationListener(
    private val converter: NotificationConverter<*>,
    private val handler: NotificationHandler<*>
) {
    init {
        if (handler.getMessageType() != converter.getMessageType()) {
            throw IllegalStateException("The registered NotificationHandler<${handler.getMessageType()}> cannot handle messages of type '${converter.getMessageType()}'.")
        }
    }

    fun convertAndHandle(message: String) {
        val notification = converter.fromMessage(message)
        @Suppress("UNCHECKED_CAST")
        (handler as NotificationHandler<Any>).handle(notification as Notification<Any>)
    }
}

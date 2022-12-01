package uk.gov.justice.digital.hmpps.messaging

import uk.gov.justice.digital.hmpps.message.Notification
import kotlin.reflect.KClass

interface NotificationHandler<T : Any> {
    fun getMessageType(): KClass<T>
    fun handle(notification: Notification<T>)
}

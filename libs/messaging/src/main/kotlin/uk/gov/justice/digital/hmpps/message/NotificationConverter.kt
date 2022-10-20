package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.jms.support.converter.MessageConversionException
import org.springframework.jms.support.converter.MessageConverter
import javax.jms.Message
import javax.jms.Session
import javax.jms.TextMessage
import kotlin.reflect.KClass

abstract class NotificationConverter<T : Any>(private val objectMapper: ObjectMapper) : MessageConverter {
    protected abstract fun getMessageClass(notification: Notification<*>): KClass<T>

    override fun fromMessage(message: Message): Notification<T> {
        if (message !is TextMessage) {
            throw MessageConversionException("Unable to convert $message to a Notification")
        }

        val stringMessage = objectMapper.readValue(message.text, jacksonTypeRef<Notification<String>>())
        return Notification(
            message = objectMapper.readValue(stringMessage.message, getMessageClass(stringMessage).java),
            attributes = stringMessage.attributes
        )
    }

    override fun toMessage(notification: Any, session: Session): TextMessage {
        if (notification !is Notification<*>) {
            throw MessageConversionException("Unexpected type passed to NotificationConverter: ${notification::class}")
        }
        return session.createTextMessage(
            objectMapper.writeValueAsString(
                Notification(
                    message = objectMapper.writeValueAsString(notification.message),
                    attributes = notification.attributes
                )
            )
        )
    }
}

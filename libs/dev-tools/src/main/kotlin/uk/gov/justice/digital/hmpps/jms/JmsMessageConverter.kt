package uk.gov.justice.digital.hmpps.jms

import jakarta.jms.Message
import jakarta.jms.Session
import jakarta.jms.TextMessage
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.jms.support.converter.MessageConversionException
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.Notification

@Component
@ConditionalOnClass(ActiveMQConnectionFactory::class)
class JmsMessageConverter(private val converter: NotificationConverter<*>) : MessageConverter {
    override fun fromMessage(message: Message) = converter.fromMessage((message as TextMessage).text)

    override fun toMessage(notification: Any, session: Session): Message {
        if (notification !is Notification<*>) {
            throw MessageConversionException("Unexpected type passed to NotificationConverter: ${notification::class}")
        }
        return session.createTextMessage(converter.toMessage(notification))
    }
}

package uk.gov.justice.digital.hmpps.jms

import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.listener.NotificationListener
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import javax.jms.TextMessage

@Component
@ConditionalOnProperty("messaging.consumer.queue")
@ConditionalOnClass(ActiveMQConnectionFactory::class)
class JmsNotificationListener(
    converter: NotificationConverter<*>,
    handler: NotificationHandler<*>
) : NotificationListener(converter, handler) {

    @JmsListener(destination = "\${messaging.consumer.queue}")
    fun receive(message: TextMessage) {
        convertAndHandle(message.text)
    }
}

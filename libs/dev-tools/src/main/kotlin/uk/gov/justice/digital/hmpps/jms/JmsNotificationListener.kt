package uk.gov.justice.digital.hmpps.jms

import jakarta.jms.TextMessage
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler

@Primary
@Component
@ConditionalOnProperty("messaging.consumer.queue")
@ConditionalOnClass(ActiveMQConnectionFactory::class)
class JmsNotificationListener(
    private val handler: NotificationHandler<*>
) {
    @JmsListener(destination = "\${messaging.consumer.queue}")
    fun receive(message: TextMessage) {
        handler.handle(message.text)
    }
}

package uk.gov.justice.digital.hmpps.jms

import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import javax.jms.TextMessage

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

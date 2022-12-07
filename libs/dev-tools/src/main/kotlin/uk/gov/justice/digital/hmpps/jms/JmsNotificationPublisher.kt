package uk.gov.justice.digital.hmpps.jms

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Component
@ConditionalOnProperty("messaging.producer.topic")
@ConditionalOnClass(ActiveMQConnectionFactory::class)
class JmsNotificationPublisher(
    private val jmsTemplate: JmsTemplate,
    @Value("\${messaging.producer.topic}") private val topicName: String
) : NotificationPublisher {
    override fun publish(notification: Notification<*>) {
        jmsTemplate.convertAndSend(topicName, notification)
    }
}
